---
name: android-modern-architecture
description: Full-stack Modern Android Architecture guideline covering DDD (Domain), Clean Architecture UseCases, BaseRepository with Room DAOs/Views, MVI/UDF in Jetpack Compose, and advanced UX patterns (NuBank currency, debounced validation, global Snackbars, Navigation 3).
---

# Android Modern Architecture & UX Patterns Blueprint

This skill formalizes the complete architecture lifecycle for modern Android applications built with **Kotlin, Jetpack Compose, Navigation 3, Room, and Hilt**, enforcing strict layer boundaries (Clean Architecture & DDD) and hardened UI/UX invariants.

---

## Layer 1: Domain Layer (Pure DDD)
- **Zero Framework Dependencies:** Strictly pure Kotlin (`com.<app>.<bc>.domain.*`). No Android, Compose, or Room imports.
- **Strongly Typed Identifiers:** Value objects wrapping primitive IDs (e.g. `CuentaId(val valor: Int)`). Never use raw `Int` or `String` in domain signatures.
- **Aggregate Roots (`AggregateRoot<TId>`):** Enforce state transitions and business invariants internally via pure functions returning `Result<TAggregate>`.
- **Domain Primitives / Composite VOs:** Reusable primitives like `Dinero(val monto: Double)` and `Cantidad(val valor: Int)` with overloaded operators (`+`, `-`, `*`).
- **Repository Interface:** Pure contract `IRepository<TAggregate, TId>` defining standard CRUD and reactive `Flow` operations.

---

## Layer 2: Application Layer (Use Cases)
- **Base Contract (`BaseUseCase<P, R>`):** Enforces `protected abstract suspend fun ejecutar(params: P): Result<R>` running on `Dispatchers.Default`.
- **Zero Synthetic Exception Throwing:** Business failures and missing entity lookups return `Result.failure(error)` rather than throwing unchecked exceptions.
- **Parameter DTOs:** Pure data classes (e.g., `RegistrarVentaParams`) capturing user intent without domain entities.

---

## Layer 3: Data & Persistence Layer (Room + BaseRepository)
- **Decoupled Entities & Mappers:** Room `@Entity` and `@DatabaseView` models mapped to/from domain aggregates using `IMapper<TAggregate, TPersistence>`.
- **Generic `BaseRepository<TAggregate, TId, TPersistence, TDao>`:**
  - Encapsulates template methods for CRUD: `onHydrateQuery`, `onGetAllQuery`, `onHydrateAggregate`.
  - Dispatches database I/O securely via `withContext(ioDispatcher)`.
- **Complex Aggregates & Database Views (`@DatabaseView`):**
  - Use Room Views (e.g. `GetCuenta`) for parent-child joined queries with multiple tables to ensure reactive `Flow` updates when either table mutates.

---

## Layer 4: Presentation Layer (MVI & UDF in Jetpack Compose)
- **Unidirectional Data Flow (UDF):**
  - Single immutable `StateFlow<UiState>` per ViewModel.
  - User interactions dispatched as sealed interface actions: `viewModel.onAction(action)`.
  - One-shot side-effects (navigation, dialogs) dispatched via `Channel<UiEffect>`.
- **Separation of Concerns:**
  - `*Screen` (Stateful): Injects ViewModel via `@HiltViewModel`, collects state with `collectAsStateWithLifecycle()`, and routes effects.
  - `*Content` (Stateless): Accepts `(state: UiState, onAction: (UiAction) -> Unit, modifier: Modifier)`.
  - Dedicated `@PreviewLightDark` with light and dark theme fixtures.

---

## Layer 5: Modern UI/UX Component Catalog

### 1. NuBank-Style Currency Field (`CampoMoneda`)
- Digits shift automatically from right to left with fixed 2 decimals: typing `1`, `5`, `5`, `0` produces `$ 15.50`.
- Deletion removes the least significant digit seamlessly without caret jumping.

### 2. Natural Reactive Field Validation
- Error states appear only when the user finishes editing and leaves the field (`onFocusChanged / blur`) OR stops typing for a debounce threshold (`1.2s` inactivity timer).

### 3. Material 3 Date Picker (`CampoFecha`)
- Displays dates formatted as `dd-MM-yyyy` in the UI while persisting ISO `yyyy-MM-dd` in domain/data layers.

### 4. Decoupled Global Snackbar Lifecycle (`SnackbarManager`)
- Singleton `Channel<String>` buffered messaging.
- Persistent `AppScaffold` collects from `SnackbarManager.mensajes` into its `SnackbarHostState`.
- Allows screens being popped/navigated away to trigger Snackbars that appear smoothly on the destination screen without being cancelled.

### 5. Semantic Status Badges (`BadgeEstado`)
- Reusable pills with icons and semantic colors (`EXITO`, `ADVERTENCIA`, `ERROR`, `NEUTRO`).
- Darker, high-contrast tone for critical errors (`#991B1B` on `#FDE8E8`) avoiding eye strain.

### 6. Navigation 3 & Centralized TopAppBar Back Navigation
- `AppScaffold` inspects `currentRoute`. If navigating a child route (`!navigationBarScreens.contains(currentRoute)`), it dynamically displays the `AutoMirrored.Filled.ArrowBack` icon in `TopAppBar`.
- Leaf detail/form screens omit redundant nested top bars and bottom return buttons, giving 100% viewport to content.
