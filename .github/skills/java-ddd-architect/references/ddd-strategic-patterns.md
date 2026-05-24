# Padrões Estratégicos de DDD

Guia de design estratégico: como identificar e relacionar Bounded Contexts no projeto ClinicBoard.

---

## Bounded Context

Um Bounded Context é uma fronteira explícita dentro da qual um modelo de domínio específico é aplicado com consistência.

### Bounded Contexts do ClinicBoard

| Bounded Context | Responsabilidade | Microsserviço |
|----------------|-----------------|---------------|
| **Identity & Access** | Autenticação, autorização, gerenciamento de usuários/profissionais | `user-service` + `gateway` |
| **Scheduling** | Agendamentos, disponibilidade de horários, conflitos de agenda | `business-service` |
| **Patient Management** | Gerenciamento de pacientes (separação lógica dentro do business-service) | `business-service` |
| **Notification** | Envio de notificações para profissionais e/ou pacientes | `notification-service` |

---

## Mapa de Contextos (Context Map)

```
┌─────────────────────┐         ┌──────────────────────┐
│   Identity & Access │ ──OHS──▶│      Scheduling      │
│   (user-service)    │         │  (business-service)  │
└─────────────────────┘         └──────────────────────┘
                                          │
                                   Domain Event
                               (AppointmentScheduled)
                                          │
                                          ▼
                                ┌──────────────────────┐
                                │     Notification     │
                                │ (notification-svc)   │
                                └──────────────────────┘
```

---

## Padrões de Relacionamento Entre Contextos

### Open Host Service (OHS) + Published Language

**Quando usar:** O contexto provedor expõe uma API bem definida e estável para múltiplos consumidores.

**No ClinicBoard:**
- O `user-service` expõe via Feign Client um OHS para validar profissionais e recuperar seus dados.
- O `business-service` consome esse OHS através de uma **ACL** (veja abaixo) para não contaminar o modelo de domínio local com o modelo do `user-service`.

```java
// OHS — interface pública estável (no user-service)
// GET /professionals/{id}
// Response: ProfessionalPublicDTO (Published Language)

// No business-service — Porta de saída
public interface ProfessionalGateway {
    Optional<ProfessionalData> findById(ProfessionalId id);
}

// ACL — Adaptador de saída que traduz o modelo externo
@Component
public class FeignProfessionalGateway implements ProfessionalGateway {
    private final ProfessionalFeignClient feignClient;

    @Override
    public Optional<ProfessionalData> findById(ProfessionalId id) {
        // Traduz ProfessionalPublicDTO → ProfessionalData (modelo local)
        return feignClient.findById(id.value())
            .map(dto -> new ProfessionalData(new ProfessionalId(dto.id()), dto.name()));
    }
}
```

### Anti-Corruption Layer (ACL)

**Quando usar:** O modelo do contexto externo é complexo, instável ou possui conceitos conflitantes com o modelo local.

**Implementação:**
- Definir uma interface de porta de saída com o **modelo local** (sem DTOs externos).
- O adaptador de infraestrutura chama o serviço externo e traduz o modelo.
- O domínio nunca vê DTOs de outros serviços.

```java
// Conceito local — isolado do modelo externo
public record ProfessionalData(ProfessionalId id, String name) {}

// Porta de saída — usa apenas conceitos locais
public interface ProfessionalGateway {
    Optional<ProfessionalData> findById(ProfessionalId id);
}
```

### Comunicação por Eventos (Event-Driven)

**Quando usar:** O contexto consumidor só precisa reagir a algo que aconteceu; consistência eventual é aceitável.

**No ClinicBoard:**
- `business-service` publica `AppointmentScheduledEvent` → `notification-service` consome.
- O `notification-service` **não** chama o `business-service` de volta para buscar dados — o evento deve carregar todos os dados necessários para a notificação.

```
Regra: Eventos publicados para outros bounded contexts são diferentes dos Domain Events internos.
Domain Events internos → publicados em memória dentro do mesmo processo.
Integration Events → serializados e publicados no RabbitMQ para outros serviços.
```

---

## Linguagem Ubíqua por Contexto

Cada Bounded Context tem sua própria linguagem ubíqua. O mesmo conceito pode ter nomes diferentes:

| Conceito de Negócio | No `business-service` | No `notification-service` |
|--------------------|-----------------------|--------------------------|
| A pessoa atendida  | `Patient`             | `Recipient`              |
| O horário reservado| `AppointmentSlot`     | `ScheduledTime`          |
| O profissional     | `Professional`        | `Practitioner`           |

**Nunca reutilize uma classe de domínio de um contexto em outro.** Use a ACL para traduzir.

---

## Subdomínios

| Tipo | Subdomínio | Justificativa |
|------|-----------|---------------|
| **Core** | Scheduling (Agendamento) | Diferenciador do negócio; regras complexas de conflito de horário |
| **Supporting** | Patient Management | Necessário, mas sem vantagem competitiva direta |
| **Generic** | Notification, Identity | Resolvível com soluções genéricas |

**Atenção:** Invista mais design cuidadoso nos subdomínios **Core**. Para subdomínios **Generic**, use soluções prontas e simplifique.
