---
name: mvi-unidirectional-flow
description: Workflow for implementing MVI and Unidirectional Data Flow (UDF) in Jetpack Compose.
---
# MVI and Unidirectional Data Flow (UDF) Workflow

## Execution Steps
1. **State Hoisting & Unidirectional Data Flow (UDF):** Separate stateful containers from stateless presenters. Expose single, immutable UI State objects (`StateFlow<UiState>`) and collect with `collectAsStateWithLifecycle()` in the screen root.
2. **Event Handling:** Define single-source-of-truth event handlers (e.g., sealed interface UI Events) to pass user actions upward.
3. **ViewModel Integration:** ViewModels consume these UI Events, execute business logic (via UseCases), and mutate the `StateFlow<UiState>`.
