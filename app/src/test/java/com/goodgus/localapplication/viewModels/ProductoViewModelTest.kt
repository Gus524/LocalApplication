package com.goodgus.localapplication.viewModels

import com.goodgus.localapplication.core.domain.Dinero
import com.goodgus.localapplication.inventario.domain.model.EstadoProducto
import com.goodgus.localapplication.inventario.domain.model.InformacionProducto
import com.goodgus.localapplication.inventario.domain.model.Inventario
import com.goodgus.localapplication.inventario.domain.model.Producto
import com.goodgus.localapplication.inventario.domain.model.ProductoId
import com.goodgus.localapplication.inventario.domain.repository.IProductoRepository
import com.goodgus.localapplication.inventario.ui.viewModels.ProductoFormAction
import com.goodgus.localapplication.inventario.ui.viewModels.ProductoFormEffect
import com.goodgus.localapplication.inventario.ui.viewModels.ProductoViewModel
import com.goodgus.localapplication.inventario.usecase.ActualizarPrecioProductoUseCase
import com.goodgus.localapplication.inventario.usecase.RegistrarProductoUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProductoViewModelTest {

    private class FakeProductoRepository : IProductoRepository {
        val productos = mutableMapOf<Int, Producto>()
        private var idCounter = 1

        override suspend fun siguienteId(): ProductoId = ProductoId(idCounter++)
        override suspend fun obtenerPorId(id: ProductoId): Producto? = productos[id.valor]
        override suspend fun guardar(agregado: Producto): Result<Unit> {
            productos[agregado.id.valor] = agregado
            return Result.success(Unit)
        }
        override suspend fun actualizar(agregado: Producto): Result<Unit> {
            productos[agregado.id.valor] = agregado
            return Result.success(Unit)
        }
        override suspend fun eliminar(id: ProductoId): Result<Unit> {
            productos.remove(id.valor)
            return Result.success(Unit)
        }
        override suspend fun obtenerTodos(): List<Producto> = productos.values.toList()
        override suspend fun buscarPorCriterio(criterio: String): List<Producto> {
            return productos.values.filter { it.nombre.contains(criterio, ignoreCase = true) }
        }
    }

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var fakeRepository: FakeProductoRepository
    private lateinit var registrarUseCase: RegistrarProductoUseCase
    private lateinit var actualizarPrecioUseCase: ActualizarPrecioProductoUseCase
    private lateinit var viewModel: ProductoViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeProductoRepository()
        registrarUseCase = RegistrarProductoUseCase(fakeRepository)
        actualizarPrecioUseCase = ActualizarPrecioProductoUseCase(fakeRepository)
        viewModel = ProductoViewModel(registrarUseCase, actualizarPrecioUseCase, fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `OnGuardar crea nuevo producto y emite efecto NavegarAtras`() = testScope.runTest {
        viewModel.onAction(ProductoFormAction.OnNombreChange("Coca Cola 600ml"))
        viewModel.onAction(ProductoFormAction.OnMarcaChange("Coca Cola"))
        viewModel.onAction(ProductoFormAction.OnTipoChange("Refresco"))
        viewModel.onAction(ProductoFormAction.OnPrecioChange(18.5))
        viewModel.onAction(ProductoFormAction.OnStockChange(50))

        viewModel.onAction(ProductoFormAction.OnGuardar)
        advanceUntilIdle()

        assertEquals(1, fakeRepository.productos.size)
        val producto = fakeRepository.productos[1]
        assertEquals("Coca Cola 600ml", producto?.nombre)
        assertEquals(18.5, producto?.precioVenta?.monto ?: 0.0, 0.01)
        assertEquals(50, producto?.inventario?.disponibles)
        val effectReceived = viewModel.effect.first()
        assertEquals(ProductoFormEffect.NavegarAtras, effectReceived)
    }

    @Test
    fun `OnCargarProducto carga producto existente y activa modo edicion`() = testScope.runTest {
        val productoExistente = Producto(
            id = ProductoId(10),
            informacion = InformacionProducto("Papas Lays", "Lays", "Botana"),
            precioVenta = Dinero(20.0),
            inventario = Inventario(15),
            estado = EstadoProducto.ACTIVO
        )
        fakeRepository.productos[10] = productoExistente

        viewModel.onAction(ProductoFormAction.OnCargarProducto(10))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(10, state.id)
        assertEquals("Papas Lays", state.nombre)
        assertEquals(20.0, state.precioVenta, 0.01)
        assertTrue(state.esEdicion)
    }
}
