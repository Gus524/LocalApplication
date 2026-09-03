package com.goodgus.localapplication.viewModels

import android.app.Application
import com.goodgus.localapplication.common.domain.Dinero
import com.goodgus.localapplication.inventario.domain.model.EstadoProducto
import com.goodgus.localapplication.inventario.domain.model.InformacionProducto
import com.goodgus.localapplication.inventario.domain.model.Inventario
import com.goodgus.localapplication.inventario.domain.model.Producto
import com.goodgus.localapplication.inventario.domain.model.ProductoId
import com.goodgus.localapplication.inventario.domain.repository.IProductoRepository
import com.goodgus.localapplication.inventario.ui.viewModels.ProductoViewModel
import com.goodgus.localapplication.inventario.usecase.ActualizarPrecioProductoUseCase
import com.goodgus.localapplication.inventario.usecase.RegistrarProductoUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class ProductoViewModelTest {

    private class TestApp : Application()

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
    fun `guardarProducto crea nuevo producto a traves de RegistrarProductoUseCase`() = testScope.runTest {
        viewModel.updateName("Coca Cola 600ml")
        viewModel.updateMarca("Coca Cola")
        viewModel.updateTipo("Refresco")
        viewModel.updatePrecio(18.5)
        viewModel.updateCantidad(50)

        viewModel.guardarProducto()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Producto registrado correctamente", state.mensaje)
        assertFalse(state.isBusy)
        assertEquals(1, fakeRepository.productos.size)

        val productoGuardado = fakeRepository.productos[1]
        assertEquals("Coca Cola 600ml", productoGuardado?.nombre)
        assertEquals(18.5, productoGuardado?.precioVenta?.monto ?: 0.0, 0.01)
        assertEquals(50, productoGuardado?.inventario?.disponibles)
    }

    @Test
    fun `guardarProducto actualiza precio cuando isUpdate es true`() = testScope.runTest {
        val productoExistente = Producto(
            id = ProductoId(10),
            informacion = InformacionProducto("Papas Lays", "Lays", "Botana"),
            precioVenta = Dinero(20.0),
            inventario = Inventario(15),
            estado = EstadoProducto.ACTIVO
        )
        fakeRepository.productos[10] = productoExistente

        viewModel.cargarProducto("10")
        advanceUntilIdle()

        viewModel.updatePrecio(25.0)
        viewModel.guardarProducto()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Producto actualizado correctamente", state.mensaje)
        assertEquals(25.0, fakeRepository.productos[10]?.precioVenta?.monto ?: 0.0, 0.01)
    }

    @Test
    fun `cargarProducto actualiza uiState con datos del dominio`() = testScope.runTest {
        val producto = Producto(
            id = ProductoId(5),
            informacion = InformacionProducto("Galletas Oreo", "Nabisco", "Galleta"),
            precioVenta = Dinero(15.0),
            inventario = Inventario(30),
            estado = EstadoProducto.ACTIVO
        )
        fakeRepository.productos[5] = producto

        viewModel.cargarProducto("5")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("5", state.idProducto)
        assertEquals("Galletas Oreo", state.name)
        assertEquals("Nabisco", state.marca)
        assertEquals(15.0, state.precio, 0.01)
        assertEquals(30, state.cantidad)
        assertTrue(state.isUpdate)
    }
}
