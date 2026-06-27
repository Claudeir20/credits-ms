# credito-ms

![Java](https://img.shields.io/badge/Java-26-ED8B00?style=flat&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.6-6DB33F?style=flat&logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-18-4169E1?style=flat&logo=postgresql&logoColor=white)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-FF6600?style=flat&logo=rabbitmq&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=flat&logo=redis&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat&logo=docker&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-C71A36?style=flat&logo=apachemaven&logoColor=white)

Microsserviço de análise de crédito, parte de um sistema de Arquitetura Orientada a Eventos (EDA). Responsável por receber solicitações de crédito, persistir a solicitação de forma transacional e publicar eventos para os demais serviços do sistema através do RabbitMQ.

Este projeto foi construído como portfólio para vagas de desenvolvedor Java Jr, com foco no aprendizado prático de EDA, padrões de resiliência e boas práticas de domínio.

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

O `credito-ms` é o serviço produtor e consumidor central do sistema. Ele recebe a solicitação via REST, persiste no banco e publica um evento `credito.solicitado` no RabbitMQ usando o **Outbox Pattern**, garantindo que a publicação do evento nunca seja perdida mesmo em caso de falha de rede.

![Diagrama de arquitetura EDA do sistema de análise de crédito](docs/architecture.png)

> O diagrama completo do sistema, incluindo `score-ms` e `notification-ms` (em construção), está disponível em [`docs/architecture.png`](docs/architecture.png).

---

## Stack utilizada

| Tecnologia | Função |
|---|---|
| Java 26 | Linguagem |
| Spring Boot 4.0.6 | Framework principal |
| Spring Data JPA + Hibernate | Persistência |
| PostgreSQL | Banco de dados relacional |
| Flyway | Versionamento de schema |
| RabbitMQ | Message broker (Topic Exchange) |
| Redis | Cache de consultas |
| Lombok | Redução de boilerplate |
| Docker Compose | Orquestração de infraestrutura local |

---

## Conceitos aplicados

Este projeto vai além do CRUD básico, aplicando conceitos intermediários de arquitetura de sistemas distribuídos:

- **Outbox Pattern** — o evento é salvo na mesma transação do banco de dados, garantindo que a publicação no RabbitMQ nunca seja perdida mesmo se a aplicação cair entre o save e a publicação.
- **Domain-Driven Design (DDD)** — as regras de negócio vivem na entidade (`CreditRequest`), o service apenas orquestra.
- **Value Objects** — `Cpf` e `Income` são `record`s imutáveis que validam a si mesmos, impedindo a existência de um CPF ou renda inválidos no sistema.
- **Idempotência e rastreabilidade** — todo evento carrega um `correlationId`, permitindo rastrear uma solicitação do início ao fim em todos os logs do sistema.
- **Resiliência em mensageria** — retry com backoff exponencial configurado no listener, evitando reprocessamento agressivo em caso de falha temporária.
- **Cache distribuído** — consultas por CPF são cacheadas no Redis, com invalidação automática (`@CacheEvict`) sempre que uma solicitação é criada ou seu status muda.
- **Status machine via eventos** — o status da solicitação nunca é alterado diretamente pela API, apenas através de eventos consumidos do RabbitMQ.

---

## Estrutura de pacotes

```
dev.mota.credits_ms
├── config          # RabbitMQ, Redis, Jackson
├── controller       # Camada REST
├── dto              # Objetos de entrada e saída da API
├── Event
│   ├── consumed      # Eventos recebidos do score-ms
│   └── produced      # Eventos publicados pelo credito-ms
├── listener          # @RabbitListener
├── mapper            # Conversão entre entidade e DTO
├── model             # Entidades JPA
├── repository        # Interfaces JpaRepository
├── services          # Regras de orquestração
└── vo                # Value Objects (Cpf, Income)
```

---

## Modelo de dados

### `credit_requests`

| Coluna | Tipo | Descrição |
|---|---|---|
| id | UUID | Identificador da solicitação |
| cpf | VARCHAR(11) | CPF do solicitante |
| name | VARCHAR(255) | Nome do solicitante |
| income | NUMERIC(15,2) | Renda mensal |
| value_request | NUMERIC(15,2) | Valor solicitado |
| term_months | INTEGER | Prazo em meses (6 a 360) |
| status | VARCHAR(20) | PENDING, APPROVED ou REJECTED |
| correlation_id | UUID | ID de rastreabilidade do fluxo |
| created_at | TIMESTAMP | Data de criação |

### `outbox_events`

| Coluna | Tipo | Descrição |
|---|---|---|
| id | UUID | Identificador do evento |
| aggregate_id | UUID | ID da entidade de origem |
| event_type | VARCHAR(100) | Routing key do evento |
| payload | TEXT | Corpo do evento em JSON |
| correlation_id | UUID | ID de rastreabilidade |
| published | BOOLEAN | Se já foi publicado no RabbitMQ |
| created_at | TIMESTAMP | Data de criação |
| published_at | TIMESTAMP | Data da publicação |

---

## Como executar

### Pré-requisitos

- Java 21+ (testado com Java 26)
- Maven
- Docker e Docker Compose

### Subindo a infraestrutura

```bash
docker compose up -d
```

Isso inicia três containers:

| Serviço | Porta | Descrição |
|---|---|---|
| PostgreSQL | 5432 | Banco de dados |
| RabbitMQ | 5672 / 15672 | Broker e management UI |
| Redis | 6379 | Cache |

A interface de gerenciamento do RabbitMQ fica disponível em `http://localhost:15672` (usuário e senha conforme `docker-compose.yml`).

### Executando a aplicação

```bash
./mvnw spring-boot:run
```

Ou execute a classe `CreditsMsApplication` diretamente pela IDE.

O Flyway aplica as migrations automaticamente na primeira execução.

---

## Endpoints

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/v1/credits` | Cria uma nova solicitação de crédito |
| `GET` | `/api/v1/credits/{id}` | Busca uma solicitação por ID |
| `GET` | `/api/v1/credits/cpf/{cpf}` | Lista todas as solicitações de um CPF |

### Exemplo de requisição

```json
POST /api/v1/credits
Content-Type: application/json

{
  "cpf": "935.411.347-80",
  "name": "José Mota",
  "income": 5000.00,
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
| `CreditRejectEvent` | Consumido | `credit.rejected` | Recebido do `score-ms`, atualiza status para REJECTED |

Todos os eventos transitam por uma única **topic exchange** (`credit.exchange`), e todo evento carrega um `correlationId` para rastreabilidade ponta a ponta.

---

## Regras de negócio

As regras vivem na própria entidade `CreditRequest`, seguindo princípios de Domain-Driven Design:

1. O valor solicitado não pode exceder 10x a renda mensal informada.
2. O prazo deve estar entre 6 e 360 meses.
3. O CPF é validado pelo algoritmo oficial da Receita Federal (dígitos verificadores), não apenas pela quantidade de caracteres.
4. A renda mínima aceita é de R$ 1.412,00 (salário mínimo vigente).
5. O status de uma solicitação segue uma máquina de estados estrita: `PENDING → APPROVED` ou `PENDING → REJECTED`. Não é possível transicionar a partir de um estado finalizado.
6. O status nunca é alterado diretamente via API — apenas através do consumo de eventos do `score-ms`.

---

## Próximos passos

Este serviço faz parte de um sistema maior, ainda em construção:

- [ ] `score-ms` — calcula o score de crédito e publica aprovação ou rejeição
- [ ] `notification-ms` — consome o resultado final e notifica o solicitante
- [ ] Testes automatizados (unitários e de integração)
- [ ] Documentação OpenAPI/Swagger

---

## Autor

Desenvolvido por Claudeir como projeto de portfólio, com foco em Java, Spring Boot e Arquitetura Orientada a Eventos.
