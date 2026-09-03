# Contexto de Arquitectura y Blueprint de Migración a Dagger Hilt

Este documento condensa todo el estado actual del proyecto `LocalApplication` y el patrón de referencia extraído de `Aplicacion-referencia/fideicomisosappandy-qa` para migrar los **ViewModels** y **Views** a **Dagger Hilt** sin necesidad de re-explorar el código.

---

## 1. Estado Actual de la Solución (Clean Architecture + DDD)

El proyecto cuenta con 4 Bounded Contexts estructurados con desacoplamiento estricto, MVI/CQRS y 100% de tests unitarios pasando en verde (`./gradlew testDebugUnitTest`).

### A. Capa de Dominio (Pure Kotlin - Zero Android Dependencies)
* **Contratos Segregados (ISP / DRY):**
  * `IReadRepository<TAggregate, TId>` $\to$ `obtenerPorId(id)`, `obtenerTodos()`
  * `IRepository<TAggregate, TId> : IReadRepository<TAggregate, TId>` $\to$ `siguienteId()`, `guardar()`, `actualizar()`, `eliminar()`
* **Interfaces de Repositorio por BC:**
  * **Inventario:** `IProductoRepository : IRepository<Producto, ProductoId>` (+ `buscarPorCriterio(query)`)
  * **Pedidos:** `IPedidoRepository : IRepository<Pedido, PedidoId>`
  * **Compras:** `ICompraRepository : IRepository<Compra, CompraId>`
  * **Ventas:** `ICuentaRepository : IRepository<Cuenta, CuentaId>` (+ `obtenerCuentaActiva()`)
* **Casos de Uso (Commands & Queries):**
  * Inventario: `RegistrarProductoUseCase`, `ActualizarPrecioProductoUseCase`, `CambiarEstadoProductoUseCase`, `ConsultarInventarioUseCase`
  * Pedidos: `CrearPedidoUseCase`, `EditarPedidoUseCase`, `EntregarPedidoUseCase`, `CancelarPedidoUseCase`, `ConsultarPedidoUseCase`
  * Compras: `RegistrarCompraUseCase`, `CancelarCompraUseCase`, `ConsultarCompraUseCase`
  * Ventas: `RegistrarVentaUseCase`, `AbrirCuentaUseCase`, `CerrarCuentaUseCase`, `CancelarVentaUseCase`, `ConsultarCuentaUseCase`

### B. Capa de Datos (Persistencia Room + Mappers + `BaseRepository`)
* **Base Genérica:** `BaseRepository<TAggregate, TId, TPersistence, TDao : BaseDao<TPersistence>>`
* **Implementaciones Concretas:**
  * `com.goodgus.localapplication.inventario.data.repository.ProductoRepository`
  * `com.goodgus.localapplication.pedidos.data.repository.PedidoRepository`
  * `com.goodgus.localapplication.compras.data.repository.CompraRepository`
  * `com.goodgus.localapplication.ventas.data.repository.CuentaRepository`
* **Mappers:** `ProductoMapper`, `PedidoMapper`, `CompraMapper`, `CuentaMapper` (implementando `IMapper<TAggregate, TPersistence>`)
* **DAOs Room:** `ProductoDAO`, `PedidosDAO`, `CompraDAO`, `CompraProductoDAO`, `CuentaDAO`, `VentaDAO` extendiendo `BaseDao<T>`.

### C. Capa de Presentación Actual
* **Ubicación de ViewModels:**
  * `com.goodgus.localapplication.inventario.ui.viewModels.ProductoViewModel`
  * `com.goodgus.localapplication.inventario.ui.viewModels.InventarioViewModel`
  * `com.goodgus.localapplication.pedidos.ui.viewModels.PedidosViewModel`
  * `com.goodgus.localapplication.pedidos.ui.viewModels.EditPedidoViewModel`
  * `com.goodgus.localapplication.ventas.ui.viewModels.VentaViewModel`
  * `com.goodgus.localapplication.viewModels.HomeViewModel` (pendiente mover a `ventas.ui.viewModels` si se desea)
  * `com.goodgus.localapplication.compras.ui.viewModels.CompraViewModel`

---

## 2. Patrón de Referencia Analizado (`Aplicacion-referencia`)

Del análisis de `features/jubilados/ui/viewModel/JubiladosHomeViewModel.kt`, `features/jubilados/ui/view/JubiladosHomeView.kt` y `common/di/RepositoryModule.kt`:

1. **ViewModels Limpios e Independientes:**
   * Usan `@HiltViewModel`.
   * Inyectan dependencias por constructor con `@Inject constructor(...)`.
   * Heredan de `ViewModel()` (no de `AndroidViewModel`), desacoplándose del framework.
   * **Sin `companion object Factory` manual** (Hilt se encarga del ciclo de vida).
2. **Vistas Declarativas (Jetpack Compose):**
   * El Composable raíz recibe el ViewModel por defecto: `viewModel: JubiladosHomeViewModel = hiltViewModel()`.
   * El estado se observa mediante `val uiState by viewModel.uiState.collectAsStateWithLifecycle()`.
3. **Módulos Dagger / Hilt (`common.di`):**
   * `@Binds` abstracto en `RepositoryModule` para vincular interfaces de repositorio (`IRepository` / `I*Repository`) con sus clases concretas.
   * `@Provides` en `DatabaseModule` para proveer `AppDataBase` y las instancias de cada `DAO`.

---

## 3. Blueprint de Implementación para Dagger Hilt

### A. Configuración de Entrada de la Aplicación
```kotlin
// LocalApplication.kt
@HiltAndroidApp
class LocalApplication : Application()

// MainActivity.kt
@AndroidEntryPoint
class MainActivity : ComponentActivity() { ... }
```

### B. Módulos de Inyección de Dependencias (`common/di/`)

#### 1. `DatabaseModule.kt`
```kotlin
package com.goodgus.localapplication.common.di

import android.content.Context
import com.goodgus.localapplication.DAO.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDataBase {
        return AppDataBase.getInstance(context)
    }

    @Provides fun provideProductoDAO(db: AppDataBase): ProductoDAO = db.productoDao()
    @Provides fun providePedidosDAO(db: AppDataBase): PedidosDAO = db.pedidosDAO()
    @Provides fun provideCompraDAO(db: AppDataBase): CompraDAO = db.compraDAO()
    @Provides fun provideCompraProductoDAO(db: AppDataBase): CompraProductoDAO = db.compraProductoDAO()
    @Provides fun provideCuentaDAO(db: AppDataBase): CuentaDAO = db.cuentaDao()
    @Provides fun provideVentaDAO(db: AppDataBase): VentaDAO = db.ventaDao()
}
```

#### 2. `RepositoryModule.kt`
```kotlin
package com.goodgus.localapplication.common.di

import com.goodgus.localapplication.inventario.data.repository.ProductoRepository
import com.goodgus.localapplication.inventario.domain.repository.IProductoRepository
import com.goodgus.localapplication.pedidos.data.repository.PedidoRepository
import com.goodgus.localapplication.pedidos.domain.repository.IPedidoRepository
import com.goodgus.localapplication.compras.data.repository.CompraRepository
import com.goodgus.localapplication.compras.domain.repository.ICompraRepository
import com.goodgus.localapplication.ventas.data.repository.CuentaRepository
import com.goodgus.localapplication.ventas.domain.repository.ICuentaRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindProductoRepository(impl: ProductoRepository): IProductoRepository

    @Binds
    @Singleton
    abstract fun bindPedidoRepository(impl: PedidoRepository): IPedidoRepository

    @Binds
    @Singleton
    abstract fun bindCompraRepository(impl: CompraRepository): ICompraRepository

    @Binds
    @Singleton
    abstract fun bindCuentaRepository(impl: CuentaRepository): ICuentaRepository
}
```

#### 3. `DispatcherModule.kt`
```kotlin
package com.goodgus.localapplication.common.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {
    @Provides
    @Singleton
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}
```

### C. Inyección en Casos de Uso y Repositorios Concretos
Añadir `@Inject constructor(...)` en:
1. `ProductoRepository @Inject constructor(dao: ProductoDAO, ...)`
2. `PedidoRepository @Inject constructor(dao: PedidosDAO, ...)`
3. `CompraRepository @Inject constructor(dao: CompraDAO, productoDao: ProductoDAO, ...)`
4. `CuentaRepository @Inject constructor(dao: CuentaDAO, productoDao: ProductoDAO, ...)`
5. Todos los `UseCases` (`RegistrarProductoUseCase @Inject constructor(...)`, etc.).

### D. Transformación de ViewModels (Clean Hilt ViewModels)
Ejemplo de `ProductoViewModel.kt`:
```kotlin
package com.goodgus.localapplication.inventario.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goodgus.localapplication.inventario.domain.model.ProductoId
import com.goodgus.localapplication.inventario.domain.repository.IProductoRepository
import com.goodgus.localapplication.inventario.usecase.ActualizarPrecioParams
import com.goodgus.localapplication.inventario.usecase.ActualizarPrecioProductoUseCase
import com.goodgus.localapplication.inventario.usecase.RegistrarProductoParams
import com.goodgus.localapplication.inventario.usecase.RegistrarProductoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductoViewModel @Inject constructor(
    private val registrarProductoUseCase: RegistrarProductoUseCase,
    private val actualizarPrecioProductoUseCase: ActualizarPrecioProductoUseCase,
    private val productoRepository: IProductoRepository
) : ViewModel() {
    // UI State y funciones puras... (sin companion object Factory ni AndroidViewModel)
}
```

### E. Inyección en Views / Navigation
En `NavigationWrapper.kt` o directamente en las funciones Composable:
```kotlin
@Composable
fun ProductoScreen(
    idProducto: String,
    navigateBack: () -> Unit,
    viewModel: ProductoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // Renderizado UI...
}
```

---

## 4. Checklist para la Próxima Sesión

- [ ] Añadir `@HiltAndroidApp` en `LocalApplication.kt` y `@AndroidEntryPoint` en `MainActivity.kt`.
- [ ] Crear `common/di/DatabaseModule.kt`, `common/di/RepositoryModule.kt` y `common/di/DispatcherModule.kt`.
- [ ] Agregar `@Inject constructor` a los Repositorios y Casos de Uso.
- [ ] Refactorizar los 7 ViewModels para heredar de `ViewModel()`, añadir `@HiltViewModel` y eliminar los `companion object Factory`.
- [ ] Actualizar las Vistas y `NavigationWrapper.kt` usando `hiltViewModel()` y `collectAsStateWithLifecycle()`.
- [ ] Ejecutar `./gradlew testDebugUnitTest` y compilar el APK para verificar 100% de éxito.
