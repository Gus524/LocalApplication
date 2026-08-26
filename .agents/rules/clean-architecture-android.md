---
name: clean-architecture-android
description: Architectural specification of the Android system divided into Domain, Data, and Presentation layers.
trigger: always_on
---

# Overview & Topology

The system is structured into three decoupled layers with dependencies pointing inwards (Dependency Rule):

+-------------------------------------------------------------+
| Presentation Layer (Compose, ViewModels, UI State Models)   |
+-------------------------------------------------------------+
| (depends on)
v
+-------------------------------------------------------------+
| Domain Layer (UseCases, Domain Entities, Repository Ifaces) |
+-------------------------------------------------------------+
^ (depends on via DIP)
|
+-------------------------------------------------------------+
| Data Layer (DataSources, DTOs, Repository Implementations)  |
+-------------------------------------------------------------+

# Specifications & Contracts

### 1. Domain Layer
- **Content:** Use cases (`UseCase` / `Interactor`), pure business entities, and repository interface definitions.
- **Invariant:** Zero dependencies on the Android framework (`android.*`), Compose, or persistence/network libraries (Room, Retrofit, Ktor).

### 2. Data Layer
- **Content:** Repository implementations, network clients, local databases, and mappers (Mappers).
- **Mapping:** Data Transfer Objects (DTO) or Room entities must never pass directly to the domain layer; they must be mapped to Domain Entities via extension functions or explicit mappers.

### 3. Presentation Layer
- **Content:** Composables, ViewModels, UI states (`UiState`), and presentation-specific models (`UiModel`).
- **Isolation:** ViewModels only interact with Use Cases. Composables only know the `UiState` and emit action lambdas upwards.

# Invariant Constraints

1. **Unidirectional Dependency:** Upper layers know the lower ones. The Domain layer is unaware of the existence of Data or Presentation.
2. **Repository Boundary:** Repositories return Domain types (`Flow<Resource<DomainModel>>` or `DomainModel`), never network DTOs or serializable database entities.
