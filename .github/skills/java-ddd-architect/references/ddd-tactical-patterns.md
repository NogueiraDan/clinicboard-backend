# Padrões Táticos de DDD

Referência de implementação em Java 17+ dos blocos de construção táticos do Domain-Driven Design.

---

## Entidade (Entity)

Possui identidade única que persiste ao longo do tempo. Dois objetos com o mesmo ID são a mesma entidade, independentemente de seus atributos.

```java
// Entidade com identidade de negócio
public class Appointment {
    private final AppointmentId id;
    private PatientId patientId;
    private ProfessionalId professionalId;
    private AppointmentSlot slot;   // Value Object
    private AppointmentStatus status;

    // Construtor privado — use factory method ou construtor rico
    public Appointment(AppointmentId id, PatientId patientId,
                       ProfessionalId professionalId, AppointmentSlot slot) {
        Objects.requireNonNull(id, "id is required");
        Objects.requireNonNull(patientId, "patientId is required");
        this.id = id;
        this.patientId = patientId;
        this.professionalId = professionalId;
        this.slot = slot;
        this.status = AppointmentStatus.SCHEDULED;
    }

    // Comportamento rico — lógica de negócio aqui
    public void cancel() {
        if (this.status == AppointmentStatus.COMPLETED) {
            throw new DomainException("Consulta já concluída não pode ser cancelada.");
        }
        this.status = AppointmentStatus.CANCELLED;
    }

    // Sem setters públicos — mutação apenas via métodos de negócio
}
```

**Regras:**
- Identidade definida por um Value Object ID, não por primitivos.
- Estado mutável apenas via métodos que expressam intenção de negócio.
- Invariantes protegidos no próprio construtor e nos métodos de mutação.

---

## Aggregate Root

Entidade raiz que controla o acesso a todas as entidades internas do agregado. Garante consistência transacional.

```java
public class Appointment {  // Aggregate Root
    private final AppointmentId id;
    private final List<AppointmentNote> notes = new ArrayList<>(); // Entidade interna
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    // Único ponto de entrada para adicionar notas
    public void addNote(String content, ProfessionalId author) {
        if (this.status != AppointmentStatus.SCHEDULED) {
            throw new DomainException("Notas só podem ser adicionadas em consultas agendadas.");
        }
        notes.add(new AppointmentNote(AppointmentNoteId.generate(), content, author));
    }

    // Domain Event registrado dentro do agregado
    public void complete() {
        this.status = AppointmentStatus.COMPLETED;
        domainEvents.add(new AppointmentCompletedEvent(this.id, this.professionalId, this.slot));
    }

    public List<DomainEvent> pullDomainEvents() {
        var events = List.copyOf(domainEvents);
        domainEvents.clear();
        return events;
    }
}
```

**Regras:**
- Uma transação = um agregado. Nunca modificar dois agregados na mesma transação.
- Referências a outros agregados são feitas apenas pelo ID, nunca por referência direta.
- A consistência entre agregados distintos é eventual (via Domain Events ou mensageria).

---

## Value Object (Objeto de Valor)

Sem identidade. Definido por seus atributos. Imutável. Substituível.

```java
// Java 17+ — use record
public record AppointmentSlot(LocalDate date, LocalTime startTime, LocalTime endTime) {

    public AppointmentSlot {
        Objects.requireNonNull(date, "date is required");
        Objects.requireNonNull(startTime, "startTime is required");
        Objects.requireNonNull(endTime, "endTime is required");
        if (!endTime.isAfter(startTime)) {
            throw new DomainException("endTime deve ser posterior a startTime.");
        }
    }

    public boolean overlapsWith(AppointmentSlot other) {
        return this.date.equals(other.date)
            && this.startTime.isBefore(other.endTime)
            && other.startTime.isBefore(this.endTime);
    }
}

public record Email(String value) {
    public Email {
        if (value == null || !value.matches("^[\\w.+-]+@[\\w-]+\\.[\\w.]+$")) {
            throw new DomainException("Email inválido: " + value);
        }
    }
}

public record Cpf(String value) {
    public Cpf {
        // validação de CPF aqui
    }
}
```

**Regras:**
- Sempre imutável — use `record` em Java 17+.
- Validação no construtor (self-validating).
- Nunca ID gerado — VOs não têm identidade.

---

## Domain Service (Serviço de Domínio)

Lógica de domínio que não pertence naturalmente a nenhum agregado, geralmente porque envolve múltiplos agregados ou requer uma abstração de domínio.

```java
// Interface no domínio
public interface AppointmentConflictChecker {
    boolean hasConflict(ProfessionalId professionalId, AppointmentSlot slot);
}

// Domain Service — coordena regra de negócio entre agregados
public class AppointmentSchedulingService {

    private final AppointmentConflictChecker conflictChecker;

    public AppointmentSchedulingService(AppointmentConflictChecker conflictChecker) {
        this.conflictChecker = conflictChecker;
    }

    public Appointment schedule(AppointmentId id, PatientId patientId,
                                ProfessionalId professionalId, AppointmentSlot slot) {
        if (conflictChecker.hasConflict(professionalId, slot)) {
            throw new DomainException("Horário conflitante para o profissional.");
        }
        return new Appointment(id, patientId, professionalId, slot);
    }
}
```

**Regras:**
- Use somente quando a lógica genuinamente não pertence a nenhum agregado.
- Interface definida no domínio; implementação na infraestrutura (se precisar de acesso a repositório).
- Nomeie com linguagem ubíqua do contexto.

---

## Domain Event (Evento de Domínio)

Representa algo relevante que aconteceu no domínio. Imutável. Nome no passado.

```java
// Evento de domínio — imutável, no passado, com timestamp
public record AppointmentScheduledEvent(
        AppointmentId appointmentId,
        PatientId patientId,
        ProfessionalId professionalId,
        AppointmentSlot slot,
        Instant occurredAt
) implements DomainEvent {

    public AppointmentScheduledEvent(AppointmentId appointmentId, PatientId patientId,
                                     ProfessionalId professionalId, AppointmentSlot slot) {
        this(appointmentId, patientId, professionalId, slot, Instant.now());
    }
}

// Interface marcadora no domínio
public interface DomainEvent {
    Instant occurredAt();
}
```

**Regras:**
- Registrado dentro do agregado (`domainEvents.add(...)`).
- Publicado **após** a persistência, nunca antes.
- Domain Events ≠ Mensagens RabbitMQ. Eventos de domínio são internos; a publicação em mensageria é responsabilidade da infraestrutura.

---

## Repository (Porta de Saída)

Interface definida no domínio/aplicação; implementação na infraestrutura.

```java
// Porta de saída — pacote application/port/out/
public interface AppointmentRepository {
    void save(Appointment appointment);
    Optional<Appointment> findById(AppointmentId id);
    List<Appointment> findByProfessionalAndDate(ProfessionalId professionalId, LocalDate date);
}

// Implementação JPA — pacote infrastructure/adapter/out/
@Repository
public class JpaAppointmentRepository implements AppointmentRepository {
    private final AppointmentJpaRepository jpaRepository;
    private final AppointmentMapper mapper;
    // ...
}
```
