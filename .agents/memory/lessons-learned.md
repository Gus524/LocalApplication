# Lessons Learned & Architecture Memory

## 1. 4-Step DDD Engineering Lifecycle
For any new domain module, feature, or phase:
1. **Strategic Discovery & Blueprinting:** Identify Bounded Contexts, define Ubiquitous Language (Spanish business domain), propose AR, Entity, and Composite VO taxonomy in plain text before touching code (`.agents/skills/ddd-strategic-design/SKILL.md`).
2. **Business Invariant Gate:** Present structured business rules catalog (VO, Entity, AR, Exceptions) for explicit user confirmation (`.agents/skills/ddd-business-invariants/SKILL.md`).
3. **Pure Kotlin Implementation (Strict TDD):** Implement pure domain models without Android/Room framework dependencies. Base contracts: `Entity<TId>`, `AggregateRoot<TId>`, `IRepository<TAggregate, TId>`. Ensure green unit tests with edge cases.
4. **Read-Only QA Audit:** Execute `domain_quality_auditor` subagent to grade purity, language fidelity, and test edge case coverage (`.agents/skills/ddd-code-quality-audit/SKILL.md`).

## 2. Core Architectural Invariants
* **Mandatory Blueprint Gate Before Any Code Change:**
  * Before creating, modifying, or refactoring any code files (DI, UI, ViewModels, UseCases, Repositories, Navigation), ALWAYS present a complete structured blueprint and wait for explicit user validation. Never apply code modifications directly without prior blueprint presentation and user confirmation.
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

## 3. Compose UI & Component Design Standards
* **Focused, High-Signal Components (Minimal Parameter Footprint):**
  * Avoid hyper-generic composables with dozens of optional/boilerplate parameters.
  * Favor concise, purpose-built components tailored to specific UI needs (e.g. `CampoTexto(valor, onValorChange, label)`, `CampoMoneda(precio, onPrecioChange, label)`, `CampoLectura(texto, label)`, `CampoCantidad(cantidad, onCantidadChange)`).
  * Leaf and form composables must expose small, ergonomic parameter lists (3-5 focused parameters) ensuring high maintainability, readability, and zero call-site friction.

## 4. Environment & Execution Runtime
* Bazzite OS with Java 25 (`java-dev` distrobox exported to `~/.local/bin/`).
* `gradle.properties` includes `kotlin.jvm.target.validation.mode=warning` for JDK 25 compatibility.
* Test runner: `./gradlew testDebugUnitTest`.

## 5. UI/UX, Navigation & Persistence Patterns
* **Global Decoupled Snackbar Lifecycle (`SnackbarManager`):**
  * *Rejected Anti-Pattern:* Calling `snackbarHostState.showSnackbar()` inside a screen coroutine scope right before `onNavegarAtras()`. The coroutine is cancelled when the Composable leaves composition, causing dropped/lost snackbars.
  * *Corrected Behavior:* Route messages through a global decoupled `SnackbarManager` (buffered `Channel<String>`), collected continuously by the root `AppScaffold`.
* **Room `@DatabaseView` for Reactive Multi-Table Aggregates:**
  * *Rejected Anti-Pattern:* Using complex manual joins or single-table queries that do not notify Room when related child tables change.
  * *Corrected Behavior:* Model multi-table joined projections with `@DatabaseView` (e.g. `GetCuenta`), which Room monitors across all participating tables to automatically emit reactive updates through `Flow`.
* **Centralized Navigation 3 Back Navigation:**
  * *Rejected Anti-Pattern:* Adding back buttons inside the screen content area or duplicating `TopAppBar` on child screens.
  * *Corrected Behavior:* Inspect `currentRoute` in root `AppScaffold`. If the current route is not a top-level bottom-bar screen, dynamically render the `ArrowBack` navigation icon in the centralized `TopAppBar`.
* **NuBank-Style Currency Input (`CampoMoneda`):**
  * *Rejected Anti-Pattern:* Raw text fields with string-to-float parsing on comma/dot causing cursor jumps and input glitches.
  * *Corrected Behavior:* Parse input string as continuous raw integer cents and divide by 100 on each keystroke, achieving natural right-to-left decimal shift.
* **Eye-Friendly Semantic Status Badges (`BadgeEstado`):**
  * *Rejected Anti-Pattern:* Using harsh, ultra-saturated bright reds for warnings/critical inventory chips that cause visual fatigue.
  * *Corrected Behavior:* Use balanced darker tones (e.g., `#991B1B` on `#FDE8E8`) with icon + quantity and no redundant text in dense lists.

