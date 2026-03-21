# PlanTogether Common Library

> Bibliothèque Maven partagée contenant les DTOs d'événements, la configuration de sécurité, le CORS et le rate limiting communs à tous les microservices

## Rôle

Ce module Maven regroupe tout le code partagé entre les microservices PlanTogether pour éviter la duplication :
DTOs des événements RabbitMQ, configuration CORS commune, constantes de sécurité, et configuration du rate limiting
(Bucket4j + Redis). Il doit être installé après `plantogether-proto` et avant tout microservice.

## Installation

```bash
# Après plantogether-proto
cd plantogether-common
mvn clean install
```

## Contenu

### DTOs des événements RabbitMQ (`event/`)

Chaque événement suit une enveloppe standard :

```json
{
  "eventId": "uuid-v7",
  "eventType": "expense.created",
  "timestamp": "2026-03-21T10:00:00Z",
  "source": "expense-service",
  "tripId": "uuid",
  "userId": "keycloak-uuid",
  "payload": { ... }
}
```

| Classe | Routing Key | Producteur |
|--------|-------------|------------|
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
| `UserProfileUpdatedEvent` | `user.profile.updated` | keycloak-spi |
| `UserDeletedEvent` | `user.deleted` | keycloak-spi |

### Configuration de sécurité (`security/`)

- `KeycloakJwtConverter` : mappe `realm_access.roles` → Spring `ROLE_<ROLE>` authorities
- Constantes : noms des rôles (`ROLE_ORGANIZER`, `ROLE_PARTICIPANT`)
- Configuration Spring Security OAuth2 Resource Server partagée

### CORS (`config/`)

- `CorsConfig` : politique CORS commune importée par chaque service
  - Origines : `https://app.plantogether.com`, `http://localhost:*` (dev)
  - Méthodes : GET, POST, PUT, PATCH, DELETE, OPTIONS
  - Headers : `Authorization`, `Content-Type`, `X-Request-ID`
  - `credentials: true`

### Rate Limiting (`ratelimit/`)

- `RateLimitConfig` : configuration Bucket4j + Redis partagée
- Limite globale : 100 requêtes / minute / utilisateur (extrait du JWT)
- Les limites spécifiques par endpoint sont configurées dans chaque service

### Exceptions (`exception/`)

- `ResourceNotFoundException` (404)
- `ForbiddenException` (403)
- `ConflictException` (409)
- `ProblemDetailExceptionHandler` : formatte toutes les erreurs en `ProblemDetail` (RFC 9457)

## Structure du module

```
plantogether-common/
├── src/main/java/com/plantogether/common/
│   ├── event/                  # DTOs événements RabbitMQ (Java Records)
│   │   ├── TripCreatedEvent.java
│   │   ├── ExpenseCreatedEvent.java
│   │   └── ...
│   ├── security/
│   │   ├── KeycloakJwtConverter.java
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

## Dépendances vers ce module

Tous les microservices métier dépendent de `plantogether-common` :

```xml
<dependency>
    <groupId>com.plantogether</groupId>
    <artifactId>plantogether-common</artifactId>
    <version>${project.version}</version>
</dependency>
```
