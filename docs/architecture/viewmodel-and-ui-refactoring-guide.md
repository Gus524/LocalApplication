# Guía Arquitectónica y Análisis Comparativo: Refactorización de ViewModels, Componentes y Vistas en Jetpack Compose

## 1. Contexto y Objetivos

Con la migración del dominio puro (DDD) y la homogeneización de la persistencia ([`BaseRepository`](file:///home/goodgus/Documents/Github/LocalApplication/app/src/main/java/com/goodgus/localapplication/core/data/repository/BaseRepository.kt) y DAOs), la siguiente fase consiste en modernizar y estandarizar la **Capa de Presentación (ViewModels, Vistas y Componentes)**.

Este documento establece un análisis comparativo profundo entre la **aplicación actual (`LocalApplication`)** y la **aplicación de referencia (`Aplicacion-referencia`)**, integrando las directrices de Jetpack Compose moderno (`skills/compose-expert` y `skills/mvi-unidirectional-flow`).

---

## 2. Análisis Comparativo: `LocalApplication` vs `Aplicacion-referencia`

### 2.1 ViewModels y Gestión de Estado

| Criterio | Estado Actual en `LocalApplication` | Patrón en `Aplicacion-referencia` | Directriz / Buenas Prácticas Compose | Calificación y Acción |
| :--- | :--- | :--- | :--- | :--- |
| **Separación de Estados** | Estado monolítico (`ProductoUIState`, `VentaUIState`) que mezcla datos de formulario, listas de entidades, flags de UI (`lazyVisible`, `isBusy`) y mensajes. | Separa el estado en `UiState` (estado de pantalla) y `FormState` (entradas de formulario). | **Separación de responsabilidades:** `@Immutable` `UiState` para la pantalla, `FormState` o estados locales para inputs. | ⚠️ **Refactorizar:** Separar estados de formulario y estados de visualización. |
| **Eventos de Una Sola Vez (One-Shot Events)** | Se usan Strings en el estado (`mensaje: String?`, `showDelete: Boolean`) propensos a estados residuales o diálogos que no se limpian. | Utiliza `MutableSharedFlow<UiEvent>` / `asSharedFlow()` para navegación y diálogos de error (`AuthEventState`). | **Efectos laterales MVI:** `Channel<UiEffect>` o `SharedFlow` para navegación, Snackbars y alertas efímeras. | ⚠️ **Adoptar:** Implementar `sealed interface UiEvent` y canal de eventos efímeros. |
| **Recolección en Compose** | `collectAsState()` estándar de corrutinas en los composables. | `collectAsStateWithLifecycle()` de `androidx.lifecycle.compose`. | **Lifecycle-aware collection:** Evita ejecución innecesaria de flujos cuando la app está en segundo plano. | ⚠️ **Refactorizar:** Migrar 100% a `collectAsStateWithLifecycle()`. |
| **Manejo de Errores y Casos de Uso** | Métodos que ejecutan UseCases con `.fold()` pero concatenan mensajes de error genéricos a strings del estado. | `onSuccess` / `onFailure` tipados evaluando excepciones de dominio específicas (`InvalidCredentialsException`). | **Tipado Fuerte:** Mapear excepciones de dominio a eventos de UI o estados de error específicos. | ⚠️ **Refactorizar:** Unificar el manejo de `Result<T>` con eventos de error tipados. |
| **Uso de Entidades de Persistencia en UI** | Los ViewModels devuelven o exponen `models.data.Producto` (entidad Room) en su `UIState`. | Utiliza modelos de sesión y dominio desacoplados (`UserSession`, DTOs de UI). | **Aislamiento de Capas:** La UI solo debe conocer modelos de dominio (`Producto`, `Venta`) o modelos de presentación (`UiModel`), nunca `@Entity` de Room. | ⚠️ **Refactorizar:** Eliminar referencias a entidades Room en UIStates. |

---

### 2.2 Vistas (Composables de Pantalla)

| Criterio | Estado Actual en `LocalApplication` | Patrón en `Aplicacion-referencia` | Directriz Compose Expert | Calificación y Acción |
| :--- | :--- | :--- | :--- | :--- |
| **State Hoisting & Stateless Composables** | Se pasa la instancia completa del `viewModel` hacia composables hijos (`ShowCatalogo(..., viewModel)`). | Se inyecta `viewModel = hiltViewModel()` en el root y se pasan estados (`uiState`, `formState`) y callbacks hacia abajo. | **State Hoisting Invariant:** Los composables que renderizan UI deben ser 100% stateless con firma `(state: ScreenUiState, onAction: (ScreenAction) -> Unit)`. | ❌ **Anti-patrón crítico:** Eliminar el paso de `ViewModel` a composables hijos. |
| **Side Effects en la Composición** | Se llama a `viewModel.tryCuenta()` directamente dentro del cuerpo de la función `@Composable` (en `CuentaView.kt:57`). | Efectos encapsulados en `LaunchedEffect(Unit)` o en el `init` del ViewModel. | **Pureza de Composición:** Ninguna mutación o llamada a ViewModel debe ejecutarse en el cuerpo de la composición. | ❌ **Bug potencial:** Mover inicializaciones a `LaunchedEffect` o `ViewModel.init`. |
| **Cálculos y Mutaciones en `remember`** | Mutación de variables (`previousIndex = currentFirstVisibleIndex`) y `println` dentro del bloque `derivedStateOf`. | No realiza mutaciones en recomposición; delega comportamientos a modificadores y estados estándar. | **Recomposition Safety:** `derivedStateOf` debe ser puro y sin efectos secundarios. | ⚠️ **Corregir:** Limpiar `derivedStateOf` y optimizar scroll/FAB visibility. |
| **Contrato de Modifiers** | Funciones composables que no reciben `modifier: Modifier = Modifier` como primer parámetro opcional. | Composables que aceptan `modifier` y lo encadenan correctamente al nodo raíz. | **Modifier Convention:** Todo composable público debe recibir `modifier: Modifier = Modifier`. | ⚠️ **Estandarizar:** Agregar `modifier` por defecto en todos los composables. |

---

### 2.3 Componentes Reutilizables y Sistema de Diseño

| Componente | Estado Actual (`LocalApplication`) | Referencia (`Aplicacion-referencia`) | Propuesta de Unificación |
| :--- | :--- | :--- | :--- |
| **Barras Superiores (TopAppBar)** | [`TopBar.kt`](file:///home/goodgus/Documents/Github/LocalApplication/app/src/main/java/com/goodgus/localapplication/shared/components/TopBar.kt) disperso con lógica rígida. | `AppBarComponent.kt` (`AppBarBack`, `MainTopAppBar` con Slot APIs). | Crear `common/ui/components/bars/` con TopBars basadas en Slot API y Material 3. |
| **Campos de Texto** | `OutlinedTextField` crudo configurado repetidamente en cada pantalla. | `TextFieldsComponents.kt` (`CampoTexto`, `CampoPass`) con estilos y estados de error unificados. | Crear `common/ui/components/inputs/` (`AppTextField`, `AppNumberField`, `AppMoneyField`). |
| **Alertas y Diálogos** | [`ShowAlert`](file:///home/goodgus/Documents/Github/LocalApplication/app/src/main/java/com/goodgus/localapplication/shared/components/ProductoComposables.kt) acoplado a strings rígidos. | `Alerts.kt` (`SimpleAlert`, diálogos de confirmación). | Crear `common/ui/components/dialogs/` (`ConfirmDialog`, `ErrorDialog`). |
| **Loading & Overlays** | Banderas `isBusy` manejadas con `CircularProgressIndicator` dispersos. | `LoadingOverlay.kt` (Overlay modal con bloqueo de interacción y animación). | Estandarizar `LoadingOverlay()` global para estados de carga en pantalla completa. |
| **Botones y Acciones** | `Button` y `FloatingActionButton` estándar con colores en línea. | `Buttons.kt` (`GenericButton`, `AdButton`, `CerrarSesionButton`). | Estandarizar tokens de botones con feedback táctil y estados deshabilitados. |

---

## 3. Patrón Arquitectónico Objetivo (MVI / UDF Unificado)

Para cada módulo/feature, la tríada UI se compondrá de:

```
[Screen Root Composable] (Stateful)
  ├── viewModel = hiltViewModel()
  ├── uiState by viewModel.uiState.collectAsStateWithLifecycle()
  ├── LaunchedEffect (One-shot Effects / Navigation)
  └── [Screen Content Composable] (Stateless)
        ├── Parameters: state: UiState, onAction: (UiAction) -> Unit, modifier: Modifier
        └── Modular Component Hierarchy (Atoms / Molecules / Organisms)
```

### Contrato Estándar para ViewModels:
```kotlin
@HiltViewModel
class FeatureViewModel @Inject constructor(
    private val someUseCase: SomeUseCase
) : ViewModel() {

    // 1. Estado Inmutable de Pantalla
    data class FeatureUiState(
        val isLoading: Boolean = false,
        val items: List<ItemUiModel> = emptyList(),
        val errorMessage: String? = null
    )

    // 2. Acciones del Usuario (Intents)
    sealed interface FeatureAction {
        data class OnQueryChanged(val query: String) : FeatureAction
        data class OnItemClicked(val id: Int) : FeatureAction
        data object OnRefresh : FeatureAction
    }

    // 3. Eventos Efímeros de Una Sola Vez (Effects)
    sealed interface FeatureEffect {
        data class NavigateToDetail(val id: Int) : FeatureEffect
        data class ShowSnackbar(val message: String) : FeatureEffect
    }

    private val _uiState = MutableStateFlow(FeatureUiState())
    val uiState: StateFlow<FeatureUiState> = _uiState.asStateFlow()

    private val _effect = Channel<FeatureEffect>(Channel.BUFFERED)
    val effect: Flow<FeatureEffect> = _effect.receiveAsFlow()

    fun onAction(action: FeatureAction) {
        when (action) {
            is FeatureAction.OnQueryChanged -> handleQuery(action.query)
            is FeatureAction.OnItemClicked -> _effect.trySend(FeatureEffect.NavigateToDetail(action.id))
            is FeatureAction.OnRefresh -> loadData()
        }
    }
}
```

---

## 4. Hoja de Ruta de Refactorización (Fase por Fase)

### Fase 1: Creación del Sistema de Componentes Comunes (`common/ui/components/`)
1. **Inputs:** `AppTextField`, `AppMoneyField`, `AppNumberField`.
2. **Bars:** `AppBarBack`, `AppCenterTopBar`, `AppScaffold`.
3. **Feedback:** `LoadingOverlay`, `ConfirmDialog`, `ErrorDialog`.
4. **Cards & List Items:** `ProductCard`, `SaleItemCard`, `OrderCard`.

### Fase 2: Refactorización de ViewModels a MVI / UDF
1. [`InventarioViewModel.kt`](file:///home/goodgus/Documents/Github/LocalApplication/app/src/main/java/com/goodgus/localapplication/inventario/ui/viewModels/InventarioViewModel.kt) & [`ProductoViewModel.kt`](file:///home/goodgus/Documents/Github/LocalApplication/app/src/main/java/com/goodgus/localapplication/inventario/ui/viewModels/ProductoViewModel.kt).
2. [`CuentaViewModel.kt`](file:///home/goodgus/Documents/Github/LocalApplication/app/src/main/java/com/goodgus/localapplication/ventas/ui/viewModels/CuentaViewModel.kt) & [`VentaViewModel.kt`](file:///home/goodgus/Documents/Github/LocalApplication/app/src/main/java/com/goodgus/localapplication/ventas/ui/viewModels/VentaViewModel.kt).
3. [`CompraViewModel.kt`](file:///home/goodgus/Documents/Github/LocalApplication/app/src/main/java/com/goodgus/localapplication/compras/ui/viewModels/CompraViewModel.kt).
4. [`PedidosViewModel.kt`](file:///home/goodgus/Documents/Github/LocalApplication/app/src/main/java/com/goodgus/localapplication/pedidos/ui/viewModels/PedidosViewModel.kt) & [`EditPedidoViewModel.kt`](file:///home/goodgus/Documents/Github/LocalApplication/app/src/main/java/com/goodgus/localapplication/pedidos/ui/viewModels/EditPedidoViewModel.kt).

### Fase 3: Refactorización de Pantallas (Vistas Stateless + Previews)
1. Separar contenedores con estado (`*Screen`) de presentadores puros (`*Content`).
2. Migrar a `collectAsStateWithLifecycle()`.
3. Eliminar efectos en el cuerpo de composición y mutaciones en `derivedStateOf`.
4. Añadir `@PreviewLightDark` con datos de prueba.

### Fase 4: Pruebas Unitarias de ViewModels
1. Actualizar tests de ViewModels para validar transiciones de `UiState` y emisión de `UiEffect`.
2. Verificar cobertura y cero regresiones (`./gradlew testDebugUnitTest`).
