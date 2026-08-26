---
trigger: always_on
---

# User Feedback Alignment & Dynamic Memory Protocol (Android Jetpack Compose)

## Core Mandate
You operate under a strict human-in-the-loop paradigm for modern Android development. Your primary goal is aligning technical execution plans with the user's architectural guidelines (Clean Architecture, MVI/MVVM), UI/UX requirements (Material 3, edge-to-edge layouts), and idiomatic Jetpack Compose standards (state hoisting, recomposition optimization, preview-driven design). You MUST prioritize architectural determinism, idiomatic Kotlin, and lifecycle/state safety over raw generation speed.

## Protocols

### 1. Retrospective Initialization (The Memory Check)
* **Mandatory First Step:** At the start of any new session, feature planning, or refactoring task, inspect the dynamic memory file located at `.agents/memory/lessons-learned.md` (if it exists).
* Analyze past Compose anti-patterns, rejected state models (e.g., mutable state exposed from ViewModels, unsafe snapshot flows, unstable lambdas triggering unnecessary recompositions), and specific conventions recorded to maintain deterministic consistency.

### 2. The Planning Gate (Compose Blueprint & Execution Mode)
* **Direct Interactive Mode:** When receiving a request to create or refactor screens, reusable design tokens, navigation flows, or domain/data layers:
  * Present a structured blueprint outlining: **Screen State Definition (sealed interface/data class)**, **Event Handlers (sealed interface UI Events)**, **Composable Hierarchy & Modifiers**, and **ViewModel/Repository integration**.
  * Prompt the user for validation before creating or editing files (`ui/screens/**`, `ui/components/**`, `ui/theme/**`, `viewmodel/**`).
* **Delegated & Automated Mode (Pre-Approved Tasks):** When executing an already approved architectural plan or issue breakdown, proceed directly with code generation adhering strictly to project standards and Android Jetpack Compose best practices.

### 3. Dynamic Learning Protocol (Lessons Learned Updates)
* Whenever the user issues a correction on Compose architecture, performance optimization, modifier ordering, or theme adherence:
  * **Action:** Append a structured entry to `.agents/memory/lessons-learned.md`.
  * **Entry Structure:**
    1. **Context/Component:** (e.g., `HomeScreen`, `State Hoisting`, `LazyColumn Keying`, `Modifier Ordering`, `Preview Annotations`).
    2. **Rejected Anti-Pattern:** (e.g., passing ViewModel into leaf Composables, missing `@Stable`/`@Immutable` markers, hardcoding dimension dp values instead of design tokens, missing key in `items()`).
    3. **Corrected Behavior:** (e.g., hoisted stateless signature `(state: HomeUiState, onAction: (HomeAction) -> Unit)`, injected `Modifier` default, localized preview parameter providers).
  * Acknowledge this update directly to the user: *"I have recorded this architectural rule in `.agents/memory/lessons-learned.md` to guarantee alignment across all upcoming Compose tasks."*

### 4. Jetpack Compose Engineering Standards
When generating or modifying Android assets, strictly enforce these core rules:
* **State Hoisting & Unidirectional Data Flow (UDF):** Separate stateful containers from stateless presenters. Expose single, immutable UI State objects (`StateFlow<UiState>`) and collect with `collectAsStateWithLifecycle()` in the screen root.
* **Slot APIs & Reusability:** Design generic components using Compose slots (`content: @Composable () -> Unit`) instead of rigid container implementations.
* **Modifier Conventions:** Every public `@Composable` component must accept an optional `modifier: Modifier = Modifier` as its first optional parameter and chain it correctly to the root node.
* **Recomposition Safety & Performance:**
  * Avoid inline object/lambda allocations inside high-frequency loops or `LazyLayout` items.
  * Always provide explicit `key` parameters for dynamic lists (`LazyColumn`, `LazyRow`).
  * Ensure data structures passed into Composables are stable (use Kotlin standard immutable collections or annotate with `@Immutable`/`@Stable` when applicable).
* **Previews & Tooling:** Provide comprehensive `@Preview` definitions (including Light/Dark theme and multi-device configurations) using `@PreviewLightDark` and explicit `PreviewParameterProvider` fixtures where appropriate.
