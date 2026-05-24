# Comunicação Entre Microsserviços

Guia de decisão e implementação para comunicação síncrona e assíncrona no ClinicBoard.

---

## Árvore de Decisão

```
O consumidor precisa da resposta para CONTINUAR o caso de uso?
├── SIM → Comunicação SÍNCRONA (Feign Client + Resilience4j)
│         Ex: Validar se profissional existe antes de criar agendamento
└── NÃO → Comunicação ASSÍNCRONA (RabbitMQ + DLQ)
          Ex: Notificar paciente após agendamento confirmado
```

---

## Comunicação Síncrona — Feign Client + Resilience4j

### Quando usar
- Validações que dependem de dados de outro serviço para prosseguir.
- Leituras de dados referenciados no momento da operação.
- Tolerância a latência baixa e disponibilidade do serviço dependente esperada.

### Implementação

```java
// Feign Client — pacote infrastructure/adapter/out/
@FeignClient(
    name = "user-service",
    fallback = ProfessionalFeignClientFallback.class
)
public interface ProfessionalFeignClient {
    @GetMapping("/professionals/{id}")
    Optional<ProfessionalDTO> findById(@PathVariable String id);
}

// Fallback obrigatório
@Component
public class ProfessionalFeignClientFallback implements ProfessionalFeignClient {
    @Override
    public Optional<ProfessionalDTO> findById(String id) {
        // Log e retorno seguro — nunca lançar exceção aqui
        return Optional.empty();
    }
}
```

```yaml
# application.yml
resilience4j:
  circuitbreaker:
    instances:
      user-service:
        slidingWindowSize: 10
        failureRateThreshold: 50
        waitDurationInOpenState: 10s
        permittedNumberOfCallsInHalfOpenState: 3

feign:
  circuitbreaker:
    enabled: true
```

### Boas Práticas
- **Sempre** defina um fallback significativo; nunca deixe o Feign Client sem fallback.
- Exponha timeout explícito; não dependa do padrão infinito.
- A ACL (Anti-Corruption Layer) deve envolver o Feign Client para isolar o modelo externo.

---

## Comunicação Assíncrona — RabbitMQ + DLQ

### Quando usar
- Notificações e side effects que não bloqueiam o caso de uso principal.
- Integrações entre Bounded Contexts com consistência eventual aceitável.
- Operações que devem ser retentadas em caso de falha.

### Terminologia no ClinicBoard

| Conceito        | Propósito                                                      |
|----------------|----------------------------------------------------------------|
| **Exchange**    | Roteador de mensagens; recebe Integration Events               |
| **Queue**       | Fila de trabalho consumida pelo microsserviço                  |
| **DLQ**         | Dead Letter Queue — armazena mensagens não processadas         |
| **Routing Key** | Chave de roteamento do evento (ex: `appointment.scheduled`)    |

### Fluxo de Publicação (business-service)

```java
// Porta de saída — pacote application/port/out/
public interface AppointmentEventPublisher {
    void publishScheduled(AppointmentScheduledEvent event);
}

// Adaptador de saída — pacote infrastructure/adapter/out/
@Component
public class RabbitAppointmentEventPublisher implements AppointmentEventPublisher {
    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publishScheduled(AppointmentScheduledEvent event) {
        // Publicar APÓS a transação confirmar (usar @TransactionalEventListener)
        rabbitTemplate.convertAndSend(
            "clinicboard.exchange",
            "appointment.scheduled",
            toIntegrationEvent(event) // Converter DomainEvent → IntegrationEvent (DTO)
        );
    }

    private AppointmentScheduledMessage toIntegrationEvent(AppointmentScheduledEvent e) {
        return new AppointmentScheduledMessage(
            e.appointmentId().value(),
            e.patientId().value(),
            e.professionalId().value(),
            e.slot().date(),
            e.slot().startTime()
        );
    }
}
```

### Publicação Segura Pós-Transação

```java
// No caso de uso — pacote application/usecase/
@Transactional
public AppointmentId schedule(...) {
    var appointment = schedulingService.schedule(...);
    appointmentRepository.save(appointment);

    // Domain Events → Integration Events publicados após commit
    appointment.pullDomainEvents().forEach(event -> {
        if (event instanceof AppointmentScheduledEvent e) {
            eventPublisher.publishScheduled(e);
        }
    });

    return appointment.getId();
}
```

> **Alternativa recomendada:** Use `@TransactionalEventListener(phase = AFTER_COMMIT)` com `ApplicationEventPublisher` do Spring para garantir que a mensagem só seja enviada após commit.

### Configuração de DLQ

```java
// Configuração — pacote infrastructure/config/
@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue appointmentQueue() {
        return QueueBuilder.durable("clinicboard.appointment.scheduled")
            .withArgument("x-dead-letter-exchange", "clinicboard.dlq.exchange")
            .withArgument("x-dead-letter-routing-key", "appointment.scheduled.dlq")
            .build();
    }

    @Bean
    public Queue appointmentDlq() {
        return QueueBuilder.durable("clinicboard.appointment.scheduled.dlq").build();
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange("clinicboard.exchange");
    }

    @Bean
    public Binding appointmentBinding(Queue appointmentQueue, TopicExchange exchange) {
        return BindingBuilder.bind(appointmentQueue).to(exchange).with("appointment.scheduled");
    }
}
```

### Consumer com Tratamento de Erro

```java
// Adaptador de entrada — pacote infrastructure/adapter/in/ (notification-service)
@Component
@RabbitListener(queues = "clinicboard.appointment.scheduled")
public class AppointmentScheduledConsumer {

    private final SendNotificationUseCase sendNotification;

    @RabbitHandler
    public void handle(AppointmentScheduledMessage message) {
        try {
            sendNotification.execute(message);
        } catch (Exception e) {
            // Spring encaminha automaticamente para DLQ após maxAttempts
            log.error("Falha ao processar AppointmentScheduledMessage: {}", e.getMessage());
            throw e; // Re-lançar para ativar retry/DLQ
        }
    }
}
```

---

## Domain Event vs Integration Event

| | Domain Event | Integration Event |
|---|---|---|
| **Escopo** | Interno ao agregado/bounded context | Entre bounded contexts (via mensageria) |
| **Transporte** | Em memória (lista no agregado) | RabbitMQ, Kafka, etc. |
| **Modelo** | Usa tipos do domínio (VOs, IDs) | DTO serializável (primitivos) |
| **Exemplo** | `AppointmentScheduledEvent` (record de domínio) | `AppointmentScheduledMessage` (DTO com `String` e `LocalDate`) |
| **Quando publicar** | Registrado no agregado ao mudar estado | Publicado após commit da transação |

**Nunca** serializar diretamente um Domain Event para a fila. Sempre converter para um Integration Event (DTO).
