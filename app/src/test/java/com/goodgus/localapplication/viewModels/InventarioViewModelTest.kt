package com.goodgus.localapplication.viewModels

import android.app.Application
import com.goodgus.localapplication.core.domain.Dinero
import com.goodgus.localapplication.inventario.domain.model.EstadoProducto
import com.goodgus.localapplication.inventario.domain.model.InformacionProducto
import com.goodgus.localapplication.inventario.domain.model.Inventario
import com.goodgus.localapplication.inventario.domain.model.Producto
import com.goodgus.localapplication.inventario.domain.model.ProductoId
import com.goodgus.localapplication.inventario.domain.repository.IProductoRepository
import com.goodgus.localapplication.inventario.ui.viewModels.InventarioViewModel
import com.goodgus.localapplication.inventario.usecase.CambiarEstadoProductoUseCase
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InventarioViewModelTest {

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
    fun `loadProducts carga lista completa de productos desde el repositorio`() = testScope.runTest {
        viewModel.loadProducts()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.productos.size)
        assertFalse(state.isBusy)
    }

    @Test
    fun `searchProducts filtra por criterio`() = testScope.runTest {
        viewModel.updateSearch("Arroz")
        viewModel.searchProducts()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.productos.size)
        assertEquals("Arroz", state.productos[0].nombre)
    }

    @Test
    fun `downProduct desactiva producto usando CambiarEstadoProductoUseCase`() = testScope.runTest {
        viewModel.showAlert(1)
        viewModel.downProduct()
        advanceUntilIdle()

        val producto = fakeRepository.productos[1]
        assertEquals(EstadoProducto.INACTIVO, producto?.estado)
        assertEquals(false, viewModel.uiState.value.showDelete)
    }
}
