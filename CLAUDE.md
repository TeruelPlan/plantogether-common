# CLAUDE.md

This file provides guidance to Claude when working with code in this repository.

## Overview

`plantogether-common` is a shared Maven library (`com.plantogether:plantogether-common:1.0.0-SNAPSHOT`) consumed as a
dependency by all PlanTogether microservices. It contains no runnable application — only shared classes published to the
local Maven repository.

**Stack:** Java 21, Spring Boot 3.5.9 BOM (dependency management only), Lombok, Jackson (`jackson-datatype-jsr310`).

## Commands

```bash
# Build and install to local Maven repo (required before consuming services can compile)
mvn clean install

# Run tests
mvn test

# Run a single test class
mvn test -Dtest=MyClassTest

# Skip tests during install
mvn clean install -DskipTests
```

## Architecture

The library is structured around four packages under `com.plantogether.common`:

### `event`

RabbitMQ event DTOs for async inter-service communication.

All classes use `UUID` for entity IDs and `Instant` for timestamps.

**Implemented events:**
- `TripCreatedEvent` — published by trip-service on `plantogether.events` exchange, routing key `trip.created`. Fields: `tripId`, `name`, `organizerDeviceId`, `createdAt`.
- `TripDeletedEvent` — published by trip-service, routing key `trip.deleted`
- `MemberJoinedEvent` — published by trip-service, routing key `trip.member.joined`. Fields: `tripId`, `deviceId`, `joinedAt`.
- `ExpenseCreatedEvent` — published by expense-service, routing key `expense.created`

**Events to implement:**
- `PollCreatedEvent` — routing key `poll.created` (poll-service)
- `PollLockedEvent` — routing key `poll.locked` (poll-service -> notification-service + trip-service)
- `VoteCastEvent` — routing key `vote.cast` (destination-service)
- `ExpenseDeletedEvent` — routing key `expense.deleted` (expense-service)
- `TaskAssignedEvent` — routing key `task.assigned` (task-service)
- `TaskDeadlineReminderEvent` — routing key `task.deadline.reminder` (task-service scheduler)
- `ChatMessageSentEvent` — routing key `chat.message.sent` (chat-service)

All events use Exchange `plantogether.events` (Topic Exchange).

`TripEvent` is a marker interface with Jackson polymorphic deserialization (`@JsonTypeInfo` / `@JsonSubTypes`).

### `exception`

- `ResourceNotFoundException` — extends `RuntimeException`, used for 404s. Constructors: `(String message)` and
  `(String resource, Object id)`.
- `AccessDeniedException` — for 403s.
- `ErrorResponse` — Lombok `@Builder` DTO for structured error responses: `status`, `error`, `message`, `path`,
  `Instant timestamp`. Aligns with RFC 9457 ProblemDetail convention used across all services.

### `security`

- `DeviceIdFilter` — `OncePerRequestFilter` that extracts the `X-Device-Id` header, validates it as a UUID, and sets the `SecurityContext` principal. This is the **only authentication mechanism** — no JWT, no tokens.
- `SecurityAutoConfiguration` — auto-configures `SecurityFilterChain` with `DeviceIdFilter`, stateless sessions, and permitAll for actuator endpoints. Services do NOT need their own `SecurityConfig.java`.
- `SecurityConstants` — contains `DEVICE_ID_HEADER = "X-Device-Id"` and legacy JWT claim constants (kept for reference but not used by `DeviceIdFilter`).

### `grpc`

Spring Boot auto-configured gRPC client for inter-service calls to `trip-service`.

- `TripClient` — public interface with 4 methods: `isMember`, `requireMembership`, `getTripCurrency`, `getTripMembers`
- `TripGrpcClient` — canonical implementation. Constructor-injected stub, 2s deadline on every call, and a single private `handleStatusRuntimeException` helper that enforces the canonical gRPC→HTTP status mapping:
  - `UNAVAILABLE` / `DEADLINE_EXCEEDED` → `503 SERVICE_UNAVAILABLE`
  - `INTERNAL` / `UNKNOWN` → `502 BAD_GATEWAY`
  - `PERMISSION_DENIED` / `NOT_FOUND` (membership ops) → `AccessDeniedException` (403)
  - `NOT_FOUND` (data ops) → `ResourceNotFoundException` (404)
  - Anything else → `502 BAD_GATEWAY`
- `TripClientAutoConfiguration` — `@AutoConfiguration` registered in `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`. Active only when `grpc.trip-service.host` is set. Consumers need ZERO gRPC client code — just `@Autowired TripClient tripClient`.
- Value types: `TripMembership` (record), `TripMember` (record), `Role` (enum with `fromWire()` null-safe factory)

**Test support (test-jar):** `TripClientTestSupport` provides in-process gRPC-backed factories and a builder. Consumer services import via:
```xml
<dependency>
    <groupId>com.plantogether</groupId>
    <artifactId>plantogether-common</artifactId>
    <classifier>tests</classifier>
    <scope>test</scope>
</dependency>
```

### `config` (to add)

Shared configuration beans to be factorised here:
- `CorsConfig` — CORS policy shared by all services (origins: `https://app.plantogether.com`, `http://localhost:*`)
- Rate limiting configuration via Bucket4j + Redis (global: 100 req/min per deviceId)

## Key Conventions

- User identity is always referenced by **device UUID** (field names like `organizerDeviceId`,
  `deviceId`, `createdBy`) — never by Keycloak IDs or internal DB IDs.
- All entity IDs are `UUID` (prefer UUID v7 for chronological ordering).
- All timestamps are `java.time.Instant`, never `LocalDateTime`.
- All event/DTO classes use Lombok `@Data @Builder @NoArgsConstructor @AllArgsConstructor`.
- Error format: `ErrorResponse` from this library (or RFC 9457 `ProblemDetail` for new controllers).
