---

description: "Task list template for feature implementation"
---

# Tasks: [FEATURE NAME]

**Input**: Design documents from `/specs/[###-feature-name]/`

**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: The examples below include test tasks. Tests are OPTIONAL - only include them if explicitly requested in the feature specification.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Single project**: `src/`, `tests/` at repository root
- **Web app**: `backend/src/`, `frontend/src/`
- **Mobile**: `api/src/`, `ios/src/` or `android/src/`
- Paths shown below assume single project - adjust based on plan.md structure

<!--
  ============================================================================
  IMPORTANT: The tasks below are SAMPLE TASKS for illustration purposes only.

  The /speckit.tasks command MUST replace these with actual tasks based on:
  - User stories from spec.md (with their priorities P1, P2, P3...)
  - Feature requirements from plan.md
  - Entities from data-model.md
  - Endpoints from contracts/

  Tasks MUST be organized by user story so each story can be:
  - Implemented independently
  - Tested independently
  - Delivered as an MVP increment

  DO NOT keep these sample tasks in the generated tasks.md file.
  ============================================================================
-->

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure

- [ ] T001 Definir estrutura de pacotes por microsserviço impactado (`domain/`, `application/`, `infrastructure/`)
- [ ] T002 Verificar Constitution Check no `plan.md` (Princípios I–VII)
- [ ] T003 [P] Configurar dependências no `pom.xml` do microsserviço envolvido

---

## Phase 2: Domain Layer (Blocking Prerequisite — Princípio I + II)

**Purpose**: Implementar o coração do software. Nenhuma tarefa de aplicação ou
infraestrutura pode começar antes que as entidades e regras de domínio estejam
modeladas e com testes unitários passando.

**⚠️ CRÍTICO**: Sem esta fase, as demais fases NÃO podem ser iniciadas (Princípio I).

- [ ] T004 [P] Criar/atualizar Entidade ou Agregado em `domain/model/`
- [ ] T005 [P] Criar Value Objects necessários em `domain/model/` (usar Records — Princípio I)
- [ ] T006 Implementar regras de negócio na entidade/agregado
- [ ] T007 [P] Criar Domain Event em `domain/event/` (somente se necessário — Princípio I)
- [ ] T008 Escrever testes unitários do domínio (DEVEM PASSAR antes de avançar — Princípio VI)

**Checkpoint**: Domínio modelado e testado — implementação de aplicação e infraestrutura pode começar

---

## Phase 3: Application Layer (Portas e Casos de Uso — Princípio II)

**Independent Test**: [How to verify this story works on its own]

## Phase 3: Application Layer (Portas e Casos de Uso — Princípio II)

**Purpose**: Implementar as portas e os casos de uso que orquestram o domínio.

- [ ] T009 [P] Criar porta de entrada em `application/port/in/` (interface do caso de uso)
- [ ] T010 [P] Criar porta de saída em `application/port/out/` (contrato do adaptador)
- [ ] T011 Implementar caso de uso em `application/usecase/` (depende de T009, T010)
- [ ] T012 Escrever testes do caso de uso com mocks de adaptadores (Mockito — Princípio VI)

**Checkpoint**: Caso de uso implementado e testado com adaptadores mockados

---

## Phase 4: Infrastructure Layer (Adaptadores — Princípio II)

**Purpose**: Implementar os adaptadores de entrada e saída.

- [ ] T013 [P] [US1] Implementar adaptador de entrada (Controller REST) em
  `infrastructure/adapter/in/`
- [ ] T014 [P] [US1] Implementar adaptador de saída (Repository JPA) em
  `infrastructure/adapter/out/`
- [ ] T015 [US1] Configurar mapeamento de entidades JPA sem contaminar o domínio
- [ ] T016 [US1] Implementar tratamento de erros com Global Exception Handler
- [ ] T017 [US1] Registrar endpoint no Spring Actuator (Princípio V — Fluxo §7)

**Checkpoint**: User Story 1 funcionalmente completa e testável de ponta a ponta

---

## Phase 5: Integration & Security (Princípios III, IV e V)

**Purpose**: Garantir resiliência, segurança e integridade entre contextos.

- [ ] T018 [P] Configurar Feign Client com Circuit Breaker + fallback (Princípio IV)
  *(somente se houver comunicação com outro serviço)*
- [ ] T019 [P] Configurar publisher/consumer RabbitMQ + DLQ (Princípio III)
  *(somente se houver comunicação assíncrona)*
- [ ] T020 Validar autorização Spring Security para os papéis `ADMIN`/`PROFESSIONAL`
  (Princípio V)
- [ ] T021 Verificar que dados sensíveis são tratados como Value Objects (Princípio V)

**Checkpoint**: Todos os user stories funcionam de forma segura e resiliente

---

[Adicionar fases adicionais de user stories conforme necessário, seguindo o mesmo padrão]

---

## Phase N: Polish & Cross-Cutting Concerns

**Purpose**: Melhorias que afetam múltiplos user stories

- [ ] TXXX [P] Documentation updates in docs/
- [ ] TXXX Code cleanup and refactoring
- [ ] TXXX Performance optimization across all stories
- [ ] TXXX [P] Additional unit tests (if requested) in tests/unit/
- [ ] TXXX Security hardening
- [ ] TXXX Run quickstart.md validation

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3+)**: All depend on Foundational phase completion
  - User stories can then proceed in parallel (if staffed)
  - Or sequentially in priority order (P1 → P2 → P3)
- **Polish (Final Phase)**: Depends on all desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2) - No dependencies on other stories
- **User Story 2 (P2)**: Can start after Foundational (Phase 2) - May integrate with US1 but should be independently testable
- **User Story 3 (P3)**: Can start after Foundational (Phase 2) - May integrate with US1/US2 but should be independently testable

### Within Each User Story

- Tests (if included) MUST be written and FAIL before implementation
- Models before services
- Services before endpoints
- Core implementation before integration
- Story complete before moving to next priority

### Parallel Opportunities

- All Setup tasks marked [P] can run in parallel
- All Foundational tasks marked [P] can run in parallel (within Phase 2)
- Once Foundational phase completes, all user stories can start in parallel (if team capacity allows)
- All tests for a user story marked [P] can run in parallel
- Models within a story marked [P] can run in parallel
- Different user stories can be worked on in parallel by different team members

---

## Parallel Example: User Story 1

```bash
# Launch all tests for User Story 1 together (if tests requested):
Task: "Contract test for [endpoint] in tests/contract/test_[name].py"
Task: "Integration test for [user journey] in tests/integration/test_[name].py"

# Launch all models for User Story 1 together:
Task: "Create [Entity1] model in src/models/[entity1].py"
Task: "Create [Entity2] model in src/models/[entity2].py"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL - blocks all stories)
3. Complete Phase 3: User Story 1
4. **STOP and VALIDATE**: Test User Story 1 independently
5. Deploy/demo if ready

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready
2. Add User Story 1 → Test independently → Deploy/Demo (MVP!)
3. Add User Story 2 → Test independently → Deploy/Demo
4. Add User Story 3 → Test independently → Deploy/Demo
5. Each story adds value without breaking previous stories

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational together
2. Once Foundational is done:
   - Developer A: User Story 1
   - Developer B: User Story 2
   - Developer C: User Story 3
3. Stories complete and integrate independently

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Verify tests fail before implementing
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- Avoid: vague tasks, same file conflicts, cross-story dependencies that break independence
