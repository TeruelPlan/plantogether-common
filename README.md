# PlanTogether Common Library

> Bibliothèque partagée contenant les utilitaires, DTOs et logique commune pour tous les microservices

## Rôle

La librairie Common est une dépendance Maven partagée par tous les microservices de PlanTogether. Elle contient les
éléments transversaux : DTOs (Data Transfer Objects) communs, exceptions personnalisées, utilitaires de sécurité,
classes d'événements RabbitMQ et wrappers de réponse API.

### Fonctionnalités

- **DTOs partagés** : Structures de données utilisées par plusieurs services
- **Exceptions personnalisées** : Gestion d'erreurs cohérente
- **Utilitaires de sécurité** : Extraction JWT, helpers Keycloak
- **Événements RabbitMQ** : Classes pour la communication asynchrone
- **Wrappers de réponse** : ApiResponse, PageResponse pour standardiser les réponses

## Architecture

```
┌─────────────────────────────────────────────┐
│    plantogether-common-1.0.0.jar            │
│                                             │
│  ┌───────────────────────────────────────┐  │
│  │  com.plantogether.dto                 │  │
│  │  ├── TripDTO                          │  │
│  │  ├── PollDTO                          │  │
│  │  ├── DestinationDTO                   │  │
│  │  ├── ExpenseDTO                       │  │
│  │  ├── TaskDTO                          │  │
│  │  ├── ChatMessageDTO                   │  │
│  │  ├── NotificationDTO                  │  │
│  │  └── FileDTO                          │  │
│  └───────────────────────────────────────┘  │
│                                             │
│  ┌───────────────────────────────────────┐  │
│  │  com.plantogether.exception           │  │
│  │  ├── PlanTogetherException            │  │
│  │  ├── ResourceNotFoundException        │  │
│  │  ├── ForbiddenException               │  │
│  │  └── ValidationException              │  │
│  └───────────────────────────────────────┘  │
│                                             │
│  ┌───────────────────────────────────────┐  │
│  │  com.plantogether.security            │  │
│  │  ├── JwtTokenProvider                 │  │
│  │  ├── JwtClaimExtractor                │  │
│  │  └── KeycloakPrincipalHelper          │  │
│  └───────────────────────────────────────┘  │
│                                             │
│  ┌───────────────────────────────────────┐  │
│  │  com.plantogether.event               │  │
│  │  ├── TripCreatedEvent                 │  │
│  │  ├── PollLockedEvent                  │  │
│  │  ├── ExpenseCreatedEvent              │  │
│  │  ├── TaskAssignedEvent                │  │
│  │  ├── ChatMessageReceivedEvent         │  │
│  │  ├── NotificationSentEvent            │  │
│  │  └── FileUploadedEvent                │  │
│  └───────────────────────────────────────┘  │
│                                             │
│  ┌───────────────────────────────────────┐  │
│  │  com.plantogether.response            │  │
│  │  ├── ApiResponse<T>                   │  │
│  │  ├── PageResponse<T>                  │  │
│  │  └── ErrorResponse                    │  │
│  └───────────────────────────────────────┘  │
│                                             │
└─────────────────────────────────────────────┘
         ▲
         │ Dépendance Maven
         │
    ┌────┴────────────────────┐
    │  Microservices:          │
    │  - trip-service         │
    │  - poll-service         │
    │  - destination-service  │
    │  - expense-service      │
    │  - task-service         │
    │  - chat-service         │
    │  - notification-service │
    │  - file-service         │
    └─────────────────────────┘
```

## Concepts clés

### DTOs (Data Transfer Objects)

Classes simples sans logique métier, utilisées pour transférer les données entre services. Chaque DTO représente une
entité du système.

### Exceptions personnalisées

Les microservices lancent les exceptions commune qui sont converties en réponses HTTP appropriées par le gateway.

### Événements asynchrones

Classes sérializables pour les messages RabbitMQ, permettant la communication event-driven entre services.

### Wrappers de réponse

Standardisent la structure des réponses API avec statut, données, messages et pagination.

## Contenu de la librairie

### DTOs

#### TripDTO

```java
public class TripDTO {
    private Long id;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private String createdBy;
    private List<String> collaborators;
    private String status;
}
```

#### PollDTO

```java
public class PollDTO {
    private Long id;
    private Long tripId;
    private String question;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean locked;
    private List<VoteOptionDTO> options;
}
```

#### DestinationDTO

```java
public class DestinationDTO {
    private Long id;
    private Long tripId;
    private String name;
    private String coordinates;
    private String description;
    private int voteCount;
}
```

#### ExpenseDTO

```java
public class ExpenseDTO {
    private Long id;
    private Long tripId;
    private String description;
    private BigDecimal amount;
    private String currency;
    private String paidBy;
    private LocalDateTime date;
}
```

#### TaskDTO

```java
public class TaskDTO {
    private Long id;
    private Long tripId;
    private String title;
    private String description;
    private String assignedTo;
    private LocalDate dueDate;
    private String status;
}
```

#### ChatMessageDTO

```java
public class ChatMessageDTO {
    private Long id;
    private Long tripId;
    private String senderId;
    private String content;
    private LocalDateTime timestamp;
}
```

### Exceptions

#### PlanTogetherException

Exception de base pour toutes les erreurs applicatives.

```java
public class PlanTogetherException extends RuntimeException {
    private final String errorCode;
    private final int httpStatus;

    public PlanTogetherException(String message, String errorCode, int httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
}
```

#### ResourceNotFoundException

Levée quand une ressource n'est pas trouvée (404).

```java
public class ResourceNotFoundException extends PlanTogetherException {
    public ResourceNotFoundException(String resourceName, String id) {
        super(
                String.format("%s with id %s not found", resourceName, id),
                "RESOURCE_NOT_FOUND",
                404
        );
    }
}
```

#### ForbiddenException

Levée quand l'utilisateur n'a pas les permissions nécessaires (403).

```java
public class ForbiddenException extends PlanTogetherException {
    public ForbiddenException(String message) {
        super(message, "FORBIDDEN", 403);
    }
}
```

### Utilitaires de sécurité

#### JwtClaimExtractor

```java
public class JwtClaimExtractor {
    public static String extractUserId(JwtAuthenticationToken token) {
        return token.getToken().getClaimAsString("sub");
    }

    public static String extractEmail(JwtAuthenticationToken token) {
        return token.getToken().getClaimAsString("email");
    }

    public static List<String> extractRoles(JwtAuthenticationToken token) {
        return token.getToken().getClaimAsStringList("roles");
    }
}
```

#### KeycloakPrincipalHelper

```java
public class KeycloakPrincipalHelper {
    public static String getPrincipalName(Authentication authentication) {
        if (authentication instanceof KeycloakAuthenticationToken) {
            return ((KeycloakAuthenticationToken) authentication).getName();
        }
        return null;
    }
}
```

### Événements RabbitMQ

```java

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TripCreatedEvent {
    private Long tripId;
    private String title;
    private String createdBy;
    private LocalDateTime timestamp;
}

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseCreatedEvent {
    private Long expenseId;
    private Long tripId;
    private BigDecimal amount;
    private String createdBy;
    private LocalDateTime timestamp;
}

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskAssignedEvent {
    private Long taskId;
    private Long tripId;
    private String assignedTo;
    private String assignedBy;
    private LocalDateTime timestamp;
}
```

### Wrappers de réponse

#### ApiResponse<T>

```java

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Success", data, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, LocalDateTime.now());
    }
}
```

#### PageResponse<T>

```java

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResponse<T> {
    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;
}
```

## Structure du projet

```
plantogether-common/
├── pom.xml
├── src/main/java/com/plantogether/
│   ├── dto/
│   │   ├── TripDTO.java
│   │   ├── PollDTO.java
│   │   ├── DestinationDTO.java
│   │   ├── ExpenseDTO.java
│   │   ├── TaskDTO.java
│   │   ├── ChatMessageDTO.java
│   │   ├── NotificationDTO.java
│   │   └── FileDTO.java
│   ├── exception/
│   │   ├── PlanTogetherException.java
│   │   ├── ResourceNotFoundException.java
│   │   ├── ForbiddenException.java
│   │   └── ValidationException.java
│   ├── security/
│   │   ├── JwtTokenProvider.java
│   │   ├── JwtClaimExtractor.java
│   │   └── KeycloakPrincipalHelper.java
│   ├── event/
│   │   ├── TripCreatedEvent.java
│   │   ├── PollLockedEvent.java
│   │   ├── ExpenseCreatedEvent.java
│   │   ├── TaskAssignedEvent.java
│   │   ├── ChatMessageReceivedEvent.java
│   │   ├── NotificationSentEvent.java
│   │   └── FileUploadedEvent.java
│   └── response/
│       ├── ApiResponse.java
│       ├── PageResponse.java
│       └── ErrorResponse.java
└── src/test/java/com/plantogether/
    ├── dto/
    ├── exception/
    ├── security/
    └── response/
```

## Utilisation dans les microservices

### Ajouter la dépendance (pom.xml)

```xml

<dependency>
    <groupId>com.plantogether</groupId>
    <artifactId>plantogether-common</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Importer et utiliser les DTOs

```java
import com.plantogether.dto.TripDTO;
import com.plantogether.response.ApiResponse;

@RestController
@RequestMapping("/api/trips")
public class TripController {

    @GetMapping("/{id}")
    public ApiResponse<TripDTO> getTrip(@PathVariable Long id) {
        TripDTO trip = tripService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trip", id.toString()));
        return ApiResponse.success(trip);
    }
}
```

### Utiliser les exceptions

```java
public class TripService {

    public TripDTO getTrip(Long tripId, String userId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip", tripId.toString()));

        if (!trip.getCollaborators().contains(userId)) {
            throw new ForbiddenException("You don't have permission to access this trip");
        }

        return tripMapper.toDTO(trip);
    }
}
```

### Publier des événements

```java

@Component
public class TripEventPublisher {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void publishTripCreated(Trip trip) {
        TripCreatedEvent event = new TripCreatedEvent(
                trip.getId(),
                trip.getTitle(),
                trip.getCreatedBy(),
                LocalDateTime.now()
        );

        rabbitTemplate.convertAndSend("trip.exchange", "trip.created", event);
    }
}
```

## Dépendances / Prérequis

### Dépendances Maven

```xml
<!-- Lombok pour réduire le boilerplate -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>1.18.30</version>
    <scope>provided</scope>
</dependency>

        <!-- Jackson pour la sérialisation JSON -->
<dependency>
<groupId>com.fasterxml.jackson.core</groupId>
<artifactId>jackson-databind</artifactId>
<version>2.15.2</version>
</dependency>

        <!-- Spring Framework (optionnel, pour les annotations) -->
<dependency>
<groupId>org.springframework</groupId>
<artifactId>spring-context</artifactId>
<version>6.0.0</version>
<scope>provided</scope>
</dependency>

        <!-- JUnit 5 pour les tests -->
<dependency>
<groupId>org.junit.jupiter</groupId>
<artifactId>junit-jupiter</artifactId>
<version>5.9.2</version>
<scope>test</scope>
</dependency>
```

## Building et publishing

### Compiler la librairie

```bash
mvn clean install
```

### Publier sur le repository privé (optionnel)

```bash
mvn deploy -DaltReleaseDeploymentRepository=internal-releases::default::https://repo.example.com/releases \
           -DaltSnapshotDeploymentRepository=internal-snapshots::default::https://repo.example.com/snapshots
```

## Versioning

La librairie suit le versioning sémantique (Semantic Versioning) :

- **1.0.0** : Version stable initiale
- **1.1.0** : Ajout de nouvelles DTOs ou utilitaires (backward compatible)
- **2.0.0** : Changement incompatible

## Documentation supplémentaire

- [Project Lombok](https://projectlombok.org/)
- [Jackson JSON Processor](https://github.com/FasterXML/jackson)
- [Spring Framework](https://spring.io/projects/spring-framework)
- [RabbitMQ Message Patterns](https://www.rabbitmq.com/tutorials)
