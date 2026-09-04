package com.goodgus.localapplication.viewModels

import com.goodgus.localapplication.core.domain.Dinero
import com.goodgus.localapplication.inventario.domain.model.EstadoProducto
import com.goodgus.localapplication.inventario.domain.model.InformacionProducto
import com.goodgus.localapplication.inventario.domain.model.Inventario
import com.goodgus.localapplication.inventario.domain.model.Producto
import com.goodgus.localapplication.inventario.domain.model.ProductoId
import com.goodgus.localapplication.inventario.domain.repository.IProductoRepository
import com.goodgus.localapplication.inventario.ui.viewModels.InventarioAction
import com.goodgus.localapplication.inventario.ui.viewModels.InventarioEffect
import com.goodgus.localapplication.inventario.ui.viewModels.InventarioViewModel
import com.goodgus.localapplication.inventario.usecase.CambiarEstadoProductoUseCase
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InventarioViewModelTest {

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
    private lateinit var cambiarEstadoUseCase: CambiarEstadoProductoUseCase
    private lateinit var viewModel: InventarioViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeProductoRepository()
        cambiarEstadoUseCase = CambiarEstadoProductoUseCase(fakeRepository)

        fakeRepository.productos[1] = Producto(
            id = ProductoId(1),
            informacion = InformacionProducto("Arroz", "Verde Valle", "Abarrotes"),
            precioVenta = Dinero(35.0),
            inventario = Inventario(20),
            estado = EstadoProducto.ACTIVO
        )
        fakeRepository.productos[2] = Producto(
            id = ProductoId(2),
            informacion = InformacionProducto("Frijol Negro", "La Sierra", "Abarrotes"),
            precioVenta = Dinero(40.0),
            inventario = Inventario(15),
            estado = EstadoProducto.ACTIVO
        )

        viewModel = InventarioViewModel(cambiarEstadoUseCase, fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `inicializacion carga lista de productos desde el repositorio`() = testScope.runTest {
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.productos.size)
        assertEquals("Arroz", state.productos[0].nombre)
    }

    @Test
    fun `OnBuscar filtra productos segun criterio`() = testScope.runTest {
        advanceUntilIdle()
        viewModel.onAction(InventarioAction.OnBuscar("Arroz"))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.productos.size)
        assertEquals("Arroz", state.productos[0].nombre)
    }

    @Test
    fun `flujo de eliminacion desactiva producto reactivamente`() = testScope.runTest {
        advanceUntilIdle()
        viewModel.onAction(InventarioAction.OnSolicitarEliminar(1))

        assertTrue(viewModel.uiState.value.mostrarDialogoEliminar)
        assertEquals(1, viewModel.uiState.value.idProductoEliminar)

        viewModel.onAction(InventarioAction.OnConfirmarEliminar)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.mostrarDialogoEliminar)
        assertEquals(EstadoProducto.INACTIVO, fakeRepository.productos[1]?.estado)
    }

    @Test
    fun `OnCrearProducto emite efecto NavegarACrear`() = testScope.runTest {
        viewModel.onAction(InventarioAction.OnCrearProducto)
        advanceUntilIdle()

        val effectReceived = viewModel.effect.first()
        assertEquals(InventarioEffect.NavegarACrear, effectReceived)
    }
}
