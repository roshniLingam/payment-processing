# Payment Processing Service

A Spring Boot service that accepts payment requests, stores them, and processes them asynchronously. It uses a database outbox and Kafka for event delivery, with a scheduled worker for retry and recovery.

## High-level architecture

```text
Client
  │ HTTP
  ▼
Payment API ──► PostgreSQL
                  │
             Outbox publisher ──► Kafka (payment-events)
                                      │
                                      ▼
                               Kafka consumer ──┐
                                                ├─► Payment processor ──► Payment status
Scheduled retry worker ────────────────────────┘       (shared logic)       in PostgreSQL
```

When a payment is created, the service stores the payment and its outbox event in the same database transaction. A scheduled publisher sends new outbox events to Kafka. The Kafka consumer and retry worker both call the same payment processing service.

## Main components

| Component | What it does |
|---|---|
| `PaymentController` | Provides endpoints to create a payment and check its status. |
| `PaymentService` | Validates and stores payment data, applies the idempotency key, and creates the outbox event. |
| `OutboxPublisher` | Checks for new outbox events every 2 seconds and publishes them to Kafka. |
| `PaymentEventConsumer` | Reads payment events from Kafka and starts processing. |
| `PaymentRetryScheduler` | Checks for payments ready to retry every 5 seconds. |
| `PaymentExecutionService` | Runs payment processing, updates status, and schedules retries with capped exponential backoff and jitter. |
| `PaymentStateMachine` | Defines the allowed payment status transitions. |
| Repositories and entities | Store and retrieve payment and outbox data in PostgreSQL. |

## Payment lifecycle

```text
PENDING ──► PROCESSING ──► SUCCESS
               │
               └── failure ──► PENDING (retry) ──► FAILED (retry limit reached)
```

`SUCCESS` and `FAILED` are terminal states. Payment creation uses an `Idempotency-Key` header so a repeated request can return the existing payment instead of creating another one.

## API

| Method | Endpoint | Purpose |
|---|---|---|
| `POST` | `/payments` | Create a payment. Requires the `Idempotency-Key` header and a payment request body. |
| `GET` | `/payments/{paymentId}/status` | Get the current status of a payment. |

## Data model

The service stores payments and their outgoing events in PostgreSQL.

### `Payment`

| Field | Purpose |
|---|---|
| `id` | Unique payment identifier (UUID). |
| `idempotencyKey` | Prevents duplicate payments for the same request. |
| `userId`, `amount`, `currency` | Identifies the customer and payment value. |
| `status` | Lifecycle state: `PENDING`, `PROCESSING`, `SUCCESS`, or `FAILED`. |
| `providerReference` | Reference associated with the payment provider. |
| `retryCount`, `maxRetries` | Tracks processing attempts and the retry limit. |
| `nextRetryAt`, `lastAttemptAt`, `lastError` | Retry timing and most recent failure details. |
| `version` | Supports optimistic locking when concurrent updates occur. |
| `createdAt`, `updatedAt` | Record creation and last update timestamps. |

### `OutboxEvent`

| Field | Purpose |
|---|---|
| `aggregateType`, `aggregateId` | Identifies the associated payment. |
| `eventType` | Describes the event, currently `PAYMENT_CREATED`. |
| `payload` | JSON message sent to Kafka. |
| `status` | Publication state: `NEW` or `PUBLISHED`. |
| `createdAt`, `publishedAt` | Event creation and successful publication timestamps. |

## Technology stack

- **Java 21** and **Spring Boot 4**
- **Spring Web** and **Jakarta Validation** for the HTTP API and request validation
- **Spring Data JPA** and **PostgreSQL** for persistence
- **Apache Kafka** for asynchronous payment events
- **Maven** for builds and dependency management
- **Lombok** for reducing boilerplate

Redis is included as a project dependency, but the current service code does not use it. Payment provider calls are also simulated in `PaymentExecutionService`; connect a real provider there when integrating with one.

## Running locally

The application needs PostgreSQL and Kafka available and configured for the environment. Start it with the Maven wrapper:

```bash
./mvnw spring-boot:run
```

On Windows, use `mvnw.cmd spring-boot:run`.
