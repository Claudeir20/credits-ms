# credits-ms

![Java](https://img.shields.io/badge/Java-26-ED8B00?style=flat&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.6-6DB33F?style=flat&logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-18-4169E1?style=flat&logo=postgresql&logoColor=white)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-FF6600?style=flat&logo=rabbitmq&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=flat&logo=redis&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat&logo=docker&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-C71A36?style=flat&logo=apachemaven&logoColor=white)

Microsserviço de análise de crédito, parte de um sistema de Arquitetura Orientada a Eventos (EDA). Ele recebe solicitações de crédito via API REST, persiste os dados de forma transacional e publica eventos para os demais serviços do fluxo através do RabbitMQ.

O projeto foi construído como portfólio para vagas de desenvolvedor Java Jr, com foco no aprendizado prático de EDA, mensageria, resiliência e boas práticas de domínio.

---

## Sumário

- [Arquitetura](#arquitetura)
- [Stack utilizada](#stack-utilizada)
- [Conceitos aplicados](#conceitos-aplicados)
- [Estrutura de pacotes](#estrutura-de-pacotes)
- [Modelo de dados](#modelo-de-dados)
- [Como executar](#como-executar)
- [Endpoints](#endpoints)
- [Eventos publicados e consumidos](#eventos-publicados-e-consumidos)
- [Regras de negócio](#regras-de-negócio)
- [Próximos passos](#próximos-passos)

---

## Arquitetura

O `credits-ms` é o serviço responsável por receber a solicitação de crédito, persistir a solicitação no PostgreSQL e publicar o evento `credit.requested` no RabbitMQ usando o **Outbox Pattern**.

Esse desenho evita que uma solicitação seja salva no banco sem que o evento correspondente fique registrado para publicação. Se o RabbitMQ estiver indisponível no momento da criação, o evento permanece na tabela `outbox_events` e será publicado posteriormente pelo scheduler do Outbox.

![Diagrama de arquitetura EDA do sistema de análise de crédito](docs/architecture.png)

Fluxo principal:

```text
Cliente
  -> credits-ms
  -> PostgreSQL
  -> outbox_events
  -> RabbitMQ credit.exchange
  -> score-ms
  -> credit.approved / credit.rejected
  -> credits-ms atualiza status
  -> notification-ms notifica solicitante
```

---

## Stack utilizada

| Tecnologia | Função |
|---|---|
| Java 26 | Linguagem |
| Spring Boot 4.0.6 | Framework principal |
| Spring Web MVC | API REST |
| Spring Data JPA + Hibernate | Persistência |
| PostgreSQL | Banco de dados relacional |
| Flyway | Versionamento de schema |
| RabbitMQ | Message broker com Topic Exchange |
| Redis | Cache de consultas |
| Lombok | Redução de boilerplate |
| Docker Compose | Infraestrutura local |

---

## Conceitos aplicados

- **Outbox Pattern**: o evento é salvo na mesma transação da solicitação de crédito, reduzindo o risco de perda de mensagens.
- **Domain-Driven Design (DDD)**: regras de negócio concentradas na entidade `CreditRequest`.
- **Value Objects**: `Cpf` e `Income` validam seus próprios valores.
- **Eventos com rastreabilidade**: eventos carregam `correlationId` para acompanhar o fluxo entre serviços.
- **Idempotência no consumo**: eventos duplicados de aprovação ou rejeição são ignorados quando a solicitação já está no mesmo status final.
- **Resiliência em mensageria**: retry com backoff exponencial e DLQ configurados para listeners.
- **Cache distribuído**: consultas por CPF e ID são cacheadas no Redis, com invalidação quando a solicitação é criada ou atualizada.
- **Máquina de estados**: a solicitação só pode seguir de `PENDING` para `APPROVED` ou de `PENDING` para `REJECTED`.

---

## Estrutura de pacotes

```text
dev.mota.credits_ms
├── config          # RabbitMQ, Redis, Jackson
├── controller      # Camada REST
├── dto             # Objetos de entrada e saída da API
├── event
│   ├── consumed    # Eventos recebidos do score-ms
│   └── produced    # Eventos publicados pelo credits-ms
├── listener        # Consumers RabbitMQ
├── mapper          # Conversão entre entidade e DTO
├── model           # Entidades JPA
├── repository      # Interfaces JpaRepository
├── services        # Orquestração de casos de uso
└── vo              # Value Objects
```

---

## Modelo de dados

### `credit_requests`

| Coluna | Tipo | Descrição |
|---|---|---|
| id | UUID | Identificador da solicitação |
| cpf | VARCHAR(11) | CPF do solicitante |
| name | VARCHAR(255) | Nome do solicitante |
| email | VARCHAR(255) | Email usado pelo fluxo de notificação |
| income | NUMERIC(15,2) | Renda mensal |
| value_request | NUMERIC(15,2) | Valor solicitado |
| term_months | INTEGER | Prazo em meses |
| status | VARCHAR(20) | PENDING, APPROVED ou REJECTED |
| correlation_id | UUID | ID de rastreabilidade do fluxo |
| created_at | TIMESTAMP | Data de criação |

### `outbox_events`

| Coluna | Tipo | Descrição |
|---|---|---|
| id | UUID | Identificador do evento no Outbox |
| aggregate_id | UUID | ID da solicitação de crédito |
| event_type | VARCHAR(100) | Routing key do evento |
| payload | TEXT | Corpo do evento em JSON |
| correlation_id | UUID | ID de rastreabilidade |
| published | BOOLEAN | Indica se o evento já foi publicado |
| created_at | TIMESTAMP | Data de criação |
| published_at | TIMESTAMP | Data de publicação |

---

## Como executar

### Pré-requisitos

- Java 21+ (testado com Java 26)
- Maven ou Maven Wrapper
- Docker e Docker Compose

### Subindo a infraestrutura

```bash
docker compose up -d
```

Serviços iniciados:

| Serviço | Porta | Descrição |
|---|---|---|
| PostgreSQL | 5432 | Banco de dados |
| RabbitMQ | 5672 / 15672 | Broker e management UI |
| Redis | 6379 | Cache |

A interface de gerenciamento do RabbitMQ fica disponível em `http://localhost:15672`.

### Executando a aplicação

```bash
./mvnw spring-boot:run
```

No Windows:

```bash
mvnw.cmd spring-boot:run
```

O Flyway aplica as migrations automaticamente na inicialização.

---

## Endpoints

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/v1/credits` | Cria uma nova solicitação de crédito |
| `GET` | `/api/v1/credits/{id}` | Busca uma solicitação por ID |
| `GET` | `/api/v1/credits/cpf/{cpf}` | Lista solicitações por CPF |

### Exemplo de requisição

```http
POST /api/v1/credits
Content-Type: application/json
```

```json
{
  "name": "Jose Mota",
  "email": "jose@email.com",
  "cpf": "935.411.347-80",
  "income": 6500.00,
  "valueRequest": 20000.00,
  "termMonths": 24
}
```

---

## Eventos publicados e consumidos

| Evento | Direção | Routing Key | Descrição |
|---|---|---|---|
| `CreditRequestedEvent` | Publicado | `credit.requested` | Disparado após a criação de uma solicitação |
| `CreditApprovedEvent` | Consumido | `credit.approved` | Recebido do `score-ms`, atualiza status para APPROVED |
| `CreditRejectedEvent` | Consumido | `credit.rejected` | Recebido do `score-ms`, atualiza status para REJECTED |

### Evento publicado: `credit.requested`

```json
{
  "eventId": "b632c8c8-5ce2-4f4f-8f98-385c7c8f5a6f",
  "requestId": "8150262d-97b0-40f6-8f73-4c45e1f9c1c8",
  "cpf": "93541134780",
  "name": "Jose Mota",
  "email": "jose@email.com",
  "income": 6500.00,
  "valueRequest": 20000.00,
  "termMonths": 24,
  "correlationId": "c2cf0f79-71dd-4cc7-9e45-5d3bb4ea80f1",
  "occurredAt": "2026-07-08T10:00:00"
}
```

### Eventos consumidos

Routing key: `credit.approved`

```json
{
  "eventId": "d57bc450-71b2-4655-a735-256d6429cc29",
  "requestId": "8150262d-97b0-40f6-8f73-4c45e1f9c1c8",
  "name": "Jose Mota",
  "email": "jose@email.com",
  "correlationId": "c2cf0f79-71dd-4cc7-9e45-5d3bb4ea80f1",
  "occurred": "2026-07-08T10:00:01"
}
```

Routing key: `credit.rejected`

```json
{
  "eventId": "14efc8dc-f138-49f0-b2b7-b065444387a7",
  "requestId": "8150262d-97b0-40f6-8f73-4c45e1f9c1c8",
  "name": "Jose Mota",
  "email": "jose@email.com",
  "correlationId": "c2cf0f79-71dd-4cc7-9e45-5d3bb4ea80f1",
  "occurred": "2026-07-08T10:00:01"
}
```

---

## Regras de negócio

1. O valor solicitado não pode exceder 10x a renda mensal.
2. O prazo deve estar entre 6 e 360 meses.
3. O CPF é validado pelo algoritmo oficial de dígitos verificadores.
4. A renda mínima aceita é de R$ 1.412,00.
5. O status segue a máquina de estados `PENDING -> APPROVED` ou `PENDING -> REJECTED`.
6. O status não é alterado diretamente pela API, apenas por eventos vindos do `score-ms`.
7. Eventos duplicados com o mesmo resultado final são ignorados para evitar reprocessamento desnecessário.

---

## Próximos passos

- [x] Integrar com `score-ms`
- [ ] Integrar com `notification-ms`
- [ ] Criar tratamento global de exceções com `@ControllerAdvice`
- [ ] Adicionar testes de integração com infraestrutura real ou Testcontainers
- [ ] Documentar API com OpenAPI/Swagger

---

## Autor

Desenvolvido por Claudeir como projeto de portfólio, com foco em Java, Spring Boot e Arquitetura Orientada a Eventos.
