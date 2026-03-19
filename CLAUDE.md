# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

`plantogether-common` is a shared Maven library (`com.plantogether:plantogether-common:1.0.0-SNAPSHOT`) consumed as a dependency by all PlanTogether microservices. It contains no runnable application — only shared classes published to the local Maven repository.

**Stack:** Java 21, Spring Boot 3.3.6 BOM (for dependency management only), Lombok, Jackson (with `jackson-datatype-jsr310`).

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
RabbitMQ event DTOs for async inter-service communication. All classes use `UUID` for entity IDs and `Instant` for timestamps.

`TripEvent` is a marker interface with Jackson polymorphic deserialization (`@JsonTypeInfo` / `@JsonSubTypes`). Events implementing it (e.g., `TripCreatedEvent`, `TripDeletedEvent`, `MemberJoinedEvent`) carry a `"type"` field in JSON. `ExpenseCreatedEvent` does **not** implement `TripEvent` — it's a standalone event.

### `exception`
- `ResourceNotFoundException` — extends `RuntimeException`, used for 404s. Two constructors: `(String message)` and `(String resource, Object id)`.
- `AccessDeniedException` — for 403s.
- `ErrorResponse` — Lombok `@Builder` DTO for structured error responses (status, error, message, path, Instant timestamp).

### `security`
`SecurityConstants` — constants for Keycloak JWT claims (`sub`, `email`, `given_name`, `family_name`, `preferred_username`, `realm_access.roles`).

## Key Conventions

- User identity is always referenced by **Keycloak ID** (field names like `organizerKeycloakId`, `paidByKeycloakId`, `keycloakId`) — never by internal DB IDs.
- All entity IDs are `UUID`, not `Long`.
- All timestamps are `java.time.Instant`, not `LocalDateTime`.
- All event/DTO classes use Lombok `@Data @Builder @NoArgsConstructor @AllArgsConstructor`.

> **Note:** The README.md describes a broader planned API (DTOs, `ApiResponse`, `PageResponse`, additional security utilities) that does not yet exist in the codebase. The actual implemented classes are limited to the `event`, `exception`, and `security` packages above.
