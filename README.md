# PlanTogether Common Library

> Shared Maven library containing event DTOs, security configuration (DeviceIdFilter), CORS, rate limiting, and exception handling — common to all microservices

## Role

This Maven module aggregates all shared code between PlanTogether microservices to avoid duplication:
RabbitMQ event DTOs, security auto-configuration (DeviceIdFilter + SecurityFilterChain), common CORS policy,
rate limiting (Bucket4j + Redis), and exception handling. It must be installed after `plantogether-proto`
and before any microservice.

## Installation

```bash
# After plantogether-proto
cd plantogether-common
mvn clean install
```

## Contents

### RabbitMQ Event DTOs (`event/`)

Each event follows a standard envelope:

```json
{
  "eventId": "uuid-v7",
  "eventType": "expense.created",
  "timestamp": "2026-03-21T10:00:00Z",
  "source": "expense-service",
  "tripId": "uuid",
  "deviceId": "device-uuid",
  "payload": { ... }
}
```

| Class | Routing Key | Producer |
|-------|-------------|----------|
| `TripCreatedEvent` | `trip.created` | trip-service |
| `MemberJoinedEvent` | `trip.member.joined` | trip-service |
| `PollCreatedEvent` | `poll.created` | poll-service |
| `PollLockedEvent` | `poll.locked` | poll-service |
| `VoteCastEvent` | `vote.cast` | destination-service |
| `ExpenseCreatedEvent` | `expense.created` | expense-service |
| `ExpenseDeletedEvent` | `expense.deleted` | expense-service |
| `TaskAssignedEvent` | `task.assigned` | task-service |
| `TaskDeadlineReminderEvent` | `task.deadline.reminder` | task-service |
| `ChatMessageSentEvent` | `chat.message.sent` | chat-service |

### Security (`security/`)

- `DeviceIdFilter` — `OncePerRequestFilter` that extracts the `X-Device-Id` header, validates it as a UUID, and sets the SecurityContext principal. This is the **only authentication mechanism** — no JWT, no tokens, no Keycloak.
- `SecurityAutoConfiguration` — auto-configures `SecurityFilterChain` with `DeviceIdFilter`, stateless sessions, and permitAll for actuator endpoints. Services do NOT need their own `SecurityConfig.java`.
- `SecurityConstants` — contains `DEVICE_ID_HEADER = "X-Device-Id"`

### CORS (`config/`)

- `CorsConfig` — shared CORS policy imported by each service
  - Origins: `https://app.plantogether.com`, `http://localhost:*` (dev)
  - Methods: GET, POST, PUT, PATCH, DELETE, OPTIONS
  - Headers: `X-Device-Id`, `Content-Type`, `X-Request-ID`
  - `credentials: true`

### Rate Limiting (`ratelimit/`)

- `RateLimitConfig` — shared Bucket4j + Redis configuration
- Global limit: 100 requests / minute / device (extracted from `X-Device-Id` header)
- Endpoint-specific limits are configured in each service

### Exceptions (`exception/`)

- `ResourceNotFoundException` (404)
- `ForbiddenException` (403)
- `ConflictException` (409)
- `ProblemDetailExceptionHandler` — formats all errors as `ProblemDetail` (RFC 9457)

## Module Structure

```
plantogether-common/
├── src/main/java/com/plantogether/common/
│   ├── event/                  # RabbitMQ event DTOs (Java Records)
│   │   ├── TripCreatedEvent.java
│   │   ├── ExpenseCreatedEvent.java
│   │   └── ...
│   ├── security/
│   │   ├── DeviceIdFilter.java
│   │   ├── SecurityAutoConfiguration.java
│   │   └── SecurityConstants.java
│   ├── config/
│   │   └── CorsConfig.java
│   ├── ratelimit/
│   │   └── RateLimitConfig.java
│   └── exception/
│       ├── ResourceNotFoundException.java
│       ├── ForbiddenException.java
│       └── ProblemDetailExceptionHandler.java
└── pom.xml
```

## Dependency on This Module

All business microservices depend on `plantogether-common`:

```xml
<dependency>
    <groupId>com.plantogether</groupId>
    <artifactId>plantogether-common</artifactId>
    <version>${project.version}</version>
</dependency>
```
