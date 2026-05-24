# Arquitetura Hexagonal (Ports & Adapters)

Guia de implementação da Arquitetura Hexagonal no contexto do ClinicBoard.

---

## Conceito Central

O domínio e a camada de aplicação estão no centro. Tudo que é externo (banco de dados, HTTP, mensageria, serviços externos) é um detalhe de infraestrutura que se conecta ao núcleo através de **portas** (interfaces) e **adaptadores** (implementações).

```
          [REST Controller]  [RabbitMQ Consumer]
                 │                  │
            (Adapter IN)       (Adapter IN)
                 │                  │
                 ▼                  ▼
         ┌──────────────────────────────────┐
         │         PORT IN (Interface)       │ ◄── Contrato definido pelo caso de uso
         ├──────────────────────────────────┤
         │       APPLICATION LAYER          │
         │       (Use Cases)                │
         ├──────────────────────────────────┤
         │        PORT OUT (Interface)      │ ──► Contrato definido pelo caso de uso
         └──────────────────────────────────┘
                 │                  │
            (Adapter OUT)      (Adapter OUT)
                 │                  │
          [JPA Repository]   [Feign Client]   [RabbitMQ Publisher]
```

---

## Camada de Domínio (`domain/`)

**Regra absoluta:** zero dependências de framework.

```
domain/
├── model/
│   ├── Appointment.java          # Aggregate Root / Entidade
│   ├── AppointmentNote.java      # Entidade interna ao agregado
│   ├── AppointmentId.java        # Value Object (ID)
│   ├── AppointmentSlot.java      # Value Object
│   ├── AppointmentStatus.java    # Enum de domínio
│   ├── Patient.java              # Entidade / Aggregate Root
│   ├── PatientId.java            # Value Object
│   └── ProfessionalId.java       # Value Object (referência a outro BC)
├── service/
│   └── AppointmentSchedulingService.java  # Domain Service (só se necessário)
└── event/
    ├── DomainEvent.java                   # Interface marcadora
    └── AppointmentScheduledEvent.java     # Domain Event (record)
```

**Checklist da camada de domínio:**
- [ ] Nenhum import de `org.springframework.*`, `jakarta.persistence.*`, etc.
- [ ] Entidades com construtor rico e métodos de negócio.
- [ ] Value Objects como `record` com validação no construtor canônico.
- [ ] Domain Events como `record` implementando `DomainEvent`.

---

## Camada de Aplicação (`application/`)

Orquestra o domínio. Coordena transações. Define contratos (portas).

```
application/
├── port/
│   ├── in/
│   │   ├── ScheduleAppointmentUseCase.java    # Interface — contrato da porta de entrada
│   │   ├── CancelAppointmentUseCase.java
│   │   └── GetAvailableSlotsUseCase.java
│   └── out/
│       ├── AppointmentRepository.java         # Interface — contrato do repositório
│       ├── AppointmentEventPublisher.java      # Interface — contrato do publisher
│       └── ProfessionalGateway.java           # Interface — contrato do gateway externo
└── usecase/
    ├── ScheduleAppointmentUseCaseImpl.java     # Implementa ScheduleAppointmentUseCase
    └── CancelAppointmentUseCaseImpl.java
```

### Padrão de Caso de Uso

```java
// Porta de entrada — define o contrato
public interface ScheduleAppointmentUseCase {
    AppointmentId execute(ScheduleAppointmentCommand command);
}

// Command Object — carrega dados de entrada validados
public record ScheduleAppointmentCommand(
    String patientId,
    String professionalId,
    LocalDate date,
    LocalTime startTime,
    LocalTime endTime
) {}

// Caso de uso — implementa a porta de entrada
@Service
@RequiredArgsConstructor
public class ScheduleAppointmentUseCaseImpl implements ScheduleAppointmentUseCase {

    private final AppointmentRepository appointmentRepository;
    private final ProfessionalGateway professionalGateway;
    private final AppointmentEventPublisher eventPublisher;
    private final AppointmentSchedulingService schedulingService;

    @Override
    @Transactional
    public AppointmentId execute(ScheduleAppointmentCommand command) {
        var professionalId = new ProfessionalId(command.professionalId());
        var patientId = new PatientId(command.patientId());
        var slot = new AppointmentSlot(command.date(), command.startTime(), command.endTime());

        // Validação via gateway (ACL para user-service)
        professionalGateway.findById(professionalId)
            .orElseThrow(() -> new ProfessionalNotFoundException(professionalId));

        // Lógica de domínio via Domain Service
        var appointment = schedulingService.schedule(
            AppointmentId.generate(), patientId, professionalId, slot
        );

        appointmentRepository.save(appointment);

        // Publicar Integration Events após persistência
        appointment.pullDomainEvents()
            .forEach(event -> {
                if (event instanceof AppointmentScheduledEvent e) {
                    eventPublisher.publishScheduled(e);
                }
            });

        return appointment.getId();
    }
}
```

---

## Camada de Infraestrutura (`infrastructure/`)

Implementa as portas. Contém todos os detalhes tecnológicos.

```
infrastructure/
├── adapter/
│   ├── in/
│   │   ├── AppointmentController.java          # REST — chama porta de entrada
│   │   └── AppointmentScheduledConsumer.java   # RabbitMQ Consumer (se aplicável)
│   └── out/
│       ├── JpaAppointmentRepository.java       # Implementa AppointmentRepository
│       ├── AppointmentJpaRepository.java       # Spring Data JPA interface
│       ├── AppointmentEntity.java              # Entidade JPA (≠ entidade de domínio)
│       ├── AppointmentMapper.java              # Domain ↔ JPA Entity
│       ├── RabbitAppointmentEventPublisher.java # Implementa AppointmentEventPublisher
│       └── FeignProfessionalGateway.java       # Implementa ProfessionalGateway
└── config/
    ├── SecurityConfig.java
    ├── RabbitMQConfig.java
    └── FeignConfig.java
```

### Separação Entidade de Domínio × Entidade JPA

```java
// Entidade JPA — SÓ na infraestrutura
@Entity
@Table(name = "appointments")
public class AppointmentEntity {
    @Id
    private String id;
    private String patientId;
    private String professionalId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    @Enumerated(EnumType.STRING)
    private AppointmentStatus status;
}

// Mapper — converte entre domínio e persistência
@Component
public class AppointmentMapper {
    public AppointmentEntity toEntity(Appointment appointment) { ... }
    public Appointment toDomain(AppointmentEntity entity) { ... }
}

// Repositório JPA — implementa a porta de saída
@Repository
@RequiredArgsConstructor
public class JpaAppointmentRepository implements AppointmentRepository {
    private final AppointmentJpaRepository jpaRepository;
    private final AppointmentMapper mapper;

    @Override
    public void save(Appointment appointment) {
        jpaRepository.save(mapper.toEntity(appointment));
    }

    @Override
    public Optional<Appointment> findById(AppointmentId id) {
        return jpaRepository.findById(id.value()).map(mapper::toDomain);
    }
}
```

---

## Controller REST (Adaptador de Entrada)

```java
@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final ScheduleAppointmentUseCase scheduleAppointmentUseCase;
    private final CancelAppointmentUseCase cancelAppointmentUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AppointmentResponse schedule(@RequestBody @Valid ScheduleAppointmentRequest request) {
        // DTO de entrada → Command Object
        var command = new ScheduleAppointmentCommand(
            request.patientId(),
            request.professionalId(),
            request.date(),
            request.startTime(),
            request.endTime()
        );
        var id = scheduleAppointmentUseCase.execute(command);
        return new AppointmentResponse(id.value());
    }
}
```

**Regra:** O controller nunca acessa o domínio diretamente. Sempre via porta de entrada (Use Case).

---

## Regras de Dependência (Direção das Setas)

```
Controller → UseCase (Port In) → Domain
                ↑
        AppointmentRepository (Port Out)
                ↑
        JpaAppointmentRepository (Adapter Out)
```

- **Domínio** não depende de nada externo.
- **Aplicação** depende apenas do domínio e define suas próprias interfaces de porta.
- **Infraestrutura** depende da aplicação (implementa as portas) e de frameworks (Spring, JPA, etc.).
- **Nunca** a aplicação ou o domínio importam classes da infraestrutura.
