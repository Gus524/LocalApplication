# Lessons Learned & Architecture Memory

## 1. 4-Step DDD Engineering Lifecycle
For any new domain module, feature, or phase:
1. **Strategic Discovery & Blueprinting:** Identify Bounded Contexts, define Ubiquitous Language (Spanish business domain), propose AR, Entity, and Composite VO taxonomy in plain text before touching code (`.agents/skills/ddd-strategic-design/SKILL.md`).
2. **Business Invariant Gate:** Present structured business rules catalog (VO, Entity, AR, Exceptions) for explicit user confirmation (`.agents/skills/ddd-business-invariants/SKILL.md`).
3. **Pure Kotlin Implementation (Strict TDD):** Implement pure domain models without Android/Room framework dependencies. Base contracts: `Entity<TId>`, `AggregateRoot<TId>`, `IRepository<TAggregate, TId>`. Ensure green unit tests with edge cases.
4. **Read-Only QA Audit:** Execute `domain_quality_auditor` subagent to grade purity, language fidelity, and test edge case coverage (`.agents/skills/ddd-code-quality-audit/SKILL.md`).

## 2. Core Architectural Invariants
* **Ubiquitous Language & Nomenclature:**
  * Use pure Spanish business terms without noise/English words (e.g. `DetalleCompra`, `ProductoComprado`).
  * Universal domain primitives (like `Dinero` and `Cantidad`) reside in `common.domain` to be reused across bounded contexts (`compras`, `ventas`, `inventario`).
* **Strict Strong Typing (Zero Any):**
  * Prohibit `Any` across the codebase. Always use strongly typed Value Objects (e.g. `CompraId`, `PedidoId`) or explicit generic type parameters `T` where abstraction is necessary.
* **Gate Protocol (Strict Separation of Phases):**
  * Never generate or modify code files during the Business Invariant Gate (Step 2). All rules, models, and exceptions must be fully specified at a high level and explicitly approved before touching code.
* **Package by Bounded Context at Root Level:**
  * `com.goodgus.localapplication.common.domain`
  * `com.goodgus.localapplication.<bc>.domain.model`
  * `com.goodgus.localapplication.<bc>.domain.repository`
* **Pure Domain vs Use Case Separation:**
  * **Aggregate Root Invariants:** Only receive validated VOs and enforce atomic state/balance transitions in pure immutable Kotlin.
  * **Domain Exceptions:** Strictly reserved for broken domain business invariants (e.g., `CompraYaFinalizadaException`, `StockInsuficienteException`). Lookups and existence checks ("Record not found in database") belong to the Application/UseCase layer orchestration.
* **Use Cases & Application Layer Architecture:**
  * Package: `com.goodgus.localapplication.<bc>.usecase`
  * Abstract `BaseUseCase<in P, R>` in `common.usecase` enforces `protected abstract suspend fun ejecutar(params: P): Result<R>`. It acts as a safety boundary for catching unhandled domain exceptions and switching dispatchers (`Dispatchers.Default`).
  * Concrete Use Cases must never throw synthetic exceptions (`throw NoSuchElementException`, `throw IllegalStateException`). Controlled repository flows and lookups must strictly return `Result.failure(error)` early or map repository results.
  * Aggregate ID generation is delegated to `IRepository.siguienteId(): TId`. Creation Param DTOs never ask the UI for manual IDs.
* **Existing Storage & Persistence:**
  * Existing Room models (`models/data/*`) remain as internal persistence data layer, decoupled from Domain via Mappers.
  * Abstract `BaseRepository` in data layer will unify shared CRUD operations and enforce `IMapper<TDomain, TPersistence>` implementations to eliminate boilerplate.

## 3. Environment & Execution Runtime
* Bazzite OS with Java 25 (`java-dev` distrobox exported to `~/.local/bin/`).
* `gradle.properties` includes `kotlin.jvm.target.validation.mode=warning` for JDK 25 compatibility.
* Test runner: `./gradlew testDebugUnitTest`.
