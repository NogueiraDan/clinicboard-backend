<!--
SYNC IMPACT REPORT
==================
Version change: (new) → 1.0.0
Constitution criada do zero para projeto Brownfield ClinicBoard.

Modified principles: N/A (criação inicial)

Added sections:
  - Core Principles (7 princípios)
  - Pilha Tecnológica e Microsserviços
  - Fluxo de Desenvolvimento e Qualidade
  - Governance

Removed sections: N/A

Templates requiring updates:
  ✅ .specify/templates/plan-template.md — Constitution Check atualizado
  ✅ .specify/templates/spec-template.md — Constraints alinhados
  ✅ .specify/templates/tasks-template.md — Fases alinhadas aos princípios

Follow-up TODOs:
  - Nenhum placeholder intencionalmente adiado.
-->

# ClinicBoard Constitution

## Core Principles

### I. Domain-Centric (DDD) — NÃO NEGOCIÁVEL

O domínio de negócio DEVE ser o núcleo do software. Toda decisão de design começa
pelo modelo de domínio, não pelo framework ou pela infraestrutura.

- Entidades ricas, Agregados, Objetos de Valor (preferencialmente Records) e
  Serviços de Domínio DEVEM encapsular as regras de negócio.
- Bounded Contexts DEVEM ser claramente identificados. No `business-service`, os
  contextos de Pacientes (Patient) e Agendamentos (Appointment) DEVEM ser
  tratados como contextos separados com fronteiras explícitas.
- O domínio DEVE ser agnóstico a frameworks (Spring, JPA, etc.). Nenhuma
  anotação de infraestrutura é permitida na camada `domain/`.
- Domain Events DEVEM ser usados apenas para comunicar mudanças de estado
  relevantes dentro do mesmo bounded context ou para notificar outros contextos
  de forma desacoplada. Domain Events NÃO são equivalentes a mensagens RabbitMQ.

**Rationale**: Aderência à filosofia de Vlad Khononov em *Learning Domain-Driven
Design*. Sem este princípio, o sistema degenera em um modelo anêmico orientado
a CRUD sem expressividade do negócio.

### II. Arquitetura Hexagonal — NÃO NEGOCIÁVEL

O código DEVE seguir as três camadas da Arquitetura Hexagonal sem violação de
dependências entre elas.

- `domain/`: Puro Java, sem dependências externas. Contém model, service, event.
- `application/`: Casos de uso e portas (ports in/out). Depende apenas do domínio.
  Orquestra a lógica sem implementar detalhes técnicos.
- `infrastructure/`: Adaptadores (in: controllers, consumers; out: repositories,
  Feign clients, publishers). Implementa as portas definidas em `application/`.
- A Regra de Dependência DEVE fluir sempre de fora para dentro:
  `infrastructure → application → domain`. Nunca o contrário.
- Adaptadores de entrada DEVEM chamar apenas portas de entrada (`port/in`).
  Adaptadores de saída DEVEM implementar apenas portas de saída (`port/out`).

**Rationale**: Garante testabilidade, substituibilidade de adaptadores e
independência do domínio em relação a detalhes técnicos.

### III. Integridade entre Bounded Contexts

A comunicação entre contextos delimitados DEVE seguir os padrões de integração
do DDD, escolhidos conforme a natureza do relacionamento entre contextos.

- Contextos com relacionamento Conformista DEVEM usar Camada Anti-Corrupção (ACL)
  para traduzir modelos externos ao modelo interno, impedindo contaminação.
- Contextos que expõem contratos estáveis DEVEM usar Serviço de Host Aberto (OHS)
  com DTOs/contratos publicados explicitamente.
- Comunicação síncrona entre microsserviços DEVE usar Feign Client com
  Resilience4j (Circuit Breaker + fallback obrigatório).
- Comunicação assíncrona DEVE usar RabbitMQ com Dead Letter Queue (DLQ).
  Mensageria RabbitMQ é mecanismo de transporte de integração, NÃO é Domain Event.
- Eventos de integração (publicados via RabbitMQ) DEVEM ser mapeados a partir de
  Domain Events, nunca publicados diretamente de casos de uso.

**Rationale**: Evita acoplamento indevido entre serviços e corrupção do modelo
de domínio por modelos externos.

### IV. Resiliência e Comunicação entre Serviços

O sistema DEVE ser resiliente a falhas parciais entre microsserviços.

- Todo Feign Client DEVE ter um Circuit Breaker configurado via Resilience4j com
  fallback explícito implementado.
- Filas RabbitMQ DEVEM ter DLQ configurada para mensagens não processadas.
- Todos os microsserviços DEVEM se registrar no Eureka (Service Discovery).
- O API Gateway DEVE ser a única entrada pública. Chamadas diretas entre
  serviços DEVEM passar pelo Service Discovery, nunca por endereços fixos.
- Cache Redis DEVE ser usado no Gateway para validação de tokens JWT, com TTL
  alinhado ao tempo de expiração do token.

**Rationale**: Garante disponibilidade parcial do sistema mesmo quando um ou
mais microsserviços estejam indisponíveis.

### V. Segurança por Design

A segurança DEVE ser considerada desde o início de cada funcionalidade, não como
etapa posterior.

- Autenticação DEVE ser feita via JWT validado no API Gateway com cache em Redis.
- Autorização DEVE ser feita com Spring Security. O papel `ADMIN` tem acesso total;
  o papel `PROFESSIONAL` tem acesso restrito aos seus próprios dados.
- Nenhuma rota que expõe dados sensíveis de pacientes ou agendamentos DEVE ser
  acessível sem autenticação.
- O código DEVE estar livre das vulnerabilidades do OWASP Top 10. Validação de
  entrada DEVE ocorrer em todos os pontos de entrada do sistema.
- Dados sensíveis (CPF, e-mail) DEVEM ser tratados como Value Objects com
  validação embutida. Nunca trafegados como Strings brutas entre camadas.

**Rationale**: Conformidade ética e legal com dados de pacientes, exigência de
privacidade no contexto de saúde.

### VI. Testabilidade

O código DEVE ser estruturado para ser testável em múltiplas camadas.

- Testes unitários do domínio são OBRIGATÓRIOS. Toda regra de negócio em
  entidades, agregados e serviços de domínio DEVE ter cobertura de teste.
- Adaptadores DEVEM ser mockados com Mockito nos testes de unidade da camada de
  aplicação.
- Testes de integração com banco de dados e filas DEVEM usar Testcontainers.
- Nenhuma classe de domínio DEVE depender de contexto Spring para ser testada.

**Rationale**: Testes rápidos e confiáveis no domínio garantem que o núcleo do
sistema funciona independentemente da infraestrutura.

### VII. Simplicidade e YAGNI

Nenhuma complexidade DEVE ser adicionada sem justificativa clara e documentada.

- DDD tático (Agregados, Domain Events, Serviços de Domínio) DEVE ser aplicado
  apenas onde agrega valor real ao modelo de negócio, não como padrão automático.
- RabbitMQ DEVE ser usado apenas quando a comunicação assíncrona é necessária.
  Comunicação síncrona via Feign é preferível quando a consistência imediata
  é requisito.
- Abstrações e camadas intermediárias DEVEM ser justificadas. Helpers criados
  para uso único são proibidos.
- Toda complexidade adicionada DEVE ser registrada na seção "Complexity Tracking"
  do `plan.md` da feature correspondente.

**Rationale**: Evita over-engineering e mantém o código navegável à medida que
o sistema cresce.

## Pilha Tecnológica e Microsserviços

O sistema é composto pelos seguintes microsserviços, cada um com responsabilidade
única e bem definida:

| Serviço             | Responsabilidade                                      |
|---------------------|-------------------------------------------------------|
| `gateway`           | Roteamento, autenticação JWT, cache Redis             |
| `service-discovery` | Registro e descoberta de serviços (Eureka Server)     |
| `user-service`      | Gestão de usuários e profissionais, autenticação      |
| `business-service`  | Domínio central: Pacientes e Agendamentos             |
| `notification-service` | Notificações assíncronas via RabbitMQ              |

**Stack obrigatória**:
- Java 17+ com idiomas modernos (Records, Sealed Classes onde aplicável)
- Spring Boot 3.2, Spring Web, Spring Data JPA, Spring Security
- Spring Cloud: Eureka Client/Server, OpenFeign, Gateway
- Resilience4j para Circuit Breaker
- RabbitMQ com DLQ para mensageria assíncrona
- Redis para cache de tokens
- PostgreSQL como banco de dados relacional
- Docker e Docker Compose para orquestração local

**Restrições de stack**:
- Nenhuma dependência de infraestrutura (JPA, Spring, RabbitMQ) DEVE aparecer
  na camada `domain/`.
- A escolha de banco de dados e broker PODE ser substituída por adaptadores
  alternativos sem alterar o domínio.

## Fluxo de Desenvolvimento e Qualidade

Toda nova funcionalidade DEVE seguir este fluxo antes de ser considerada pronta:

1. **Especificação**: Definir casos de uso e regras de domínio impactadas.
2. **Constitution Check**: Verificar aderência aos 7 princípios antes de implementar.
3. **Domain First**: Implementar entidades, agregados e serviços de domínio.
4. **Testes de Domínio**: Testes unitários DEVEM passar antes de avançar para
   camadas externas.
5. **Portas e Adaptadores**: Implementar casos de uso, portas e adaptadores.
6. **Integração**: Verificar comunicação com outros serviços (Feign/RabbitMQ).
7. **Segurança**: Validar autorização e tratamento de dados sensíveis.

**Padrões de commits**: Conventional Commits (`feat:`, `fix:`, `refactor:`, etc.)

**Tratamento de erros**: Global Exception Handler DEVE existir em cada microsserviço
retornando respostas padronizadas (RFC 7807 Problem Details ou equivalente).

**Observabilidade**: Todo novo endpoint DEVE ser exposto via Spring Actuator e
métricas disponibilizadas em `/actuator/prometheus`.

## Governance

Esta constituição é o documento de maior autoridade do projeto ClinicBoard.
Ela DEVE ser consultada antes de qualquer decisão arquitetural ou de design.

**Regras de emenda**:
- Alterações DEVEM ser propostas com justificativa documentada.
- Toda emenda DEVE incrementar a versão conforme Semantic Versioning:
  - MAJOR: Remoção ou redefinição incompatível de princípio existente.
  - MINOR: Adição de princípio ou seção com novo conteúdo material.
  - PATCH: Clarificações, correções de redação, refinamentos sem impacto semântico.
- Após emenda, os templates dependentes em `.specify/templates/` DEVEM ser
  revisados e atualizados na mesma entrega.

**Compliance**:
- O "Constitution Check" no `plan.md` de cada feature DEVE referenciar os
  princípios desta constituição explicitamente.
- Todo PR DEVE verificar conformidade com os Princípios I, II e V como mínimo.
- Violações documentadas em "Complexity Tracking" DEVEM ter aprovação explícita
  antes de serem mergeadas.

**Version**: 1.0.0 | **Ratified**: 2026-05-24 | **Last Amended**: 2026-05-24
