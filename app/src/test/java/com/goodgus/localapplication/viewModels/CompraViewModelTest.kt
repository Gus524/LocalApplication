package com.goodgus.localapplication.viewModels

import com.goodgus.localapplication.compras.domain.model.Compra
import com.goodgus.localapplication.compras.domain.model.CompraId
import com.goodgus.localapplication.compras.domain.model.EstadoCompra
import com.goodgus.localapplication.compras.domain.model.InformacionCompra
import com.goodgus.localapplication.compras.domain.repository.ICompraRepository
import com.goodgus.localapplication.compras.ui.viewModels.CompraAction
import com.goodgus.localapplication.compras.ui.viewModels.CompraViewModel
import com.goodgus.localapplication.compras.usecase.CancelarCompraUseCase
import com.goodgus.localapplication.compras.usecase.ProductoCompraParams
import com.goodgus.localapplication.compras.usecase.RegistrarCompraParams
import com.goodgus.localapplication.compras.usecase.RegistrarCompraUseCase
import com.goodgus.localapplication.core.domain.Dinero
import com.goodgus.localapplication.inventario.domain.model.EstadoProducto
import com.goodgus.localapplication.inventario.domain.model.InformacionProducto
import com.goodgus.localapplication.inventario.domain.model.Inventario
import com.goodgus.localapplication.inventario.domain.model.Producto
import com.goodgus.localapplication.inventario.domain.model.ProductoId
import com.goodgus.localapplication.inventario.domain.repository.IProductoRepository
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CompraViewModelTest {

    private class FakeCompraRepository : ICompraRepository {
        val compras = mutableMapOf<Int, Compra>()
        private var idCounter = 1

        override suspend fun siguienteId(): CompraId = CompraId(idCounter++)
        override suspend fun obtenerPorId(id: CompraId): Compra? = compras[id.valor]
        override suspend fun guardar(agregado: Compra): Result<Unit> {
            compras[agregado.id.valor] = agregado
            return Result.success(Unit)
        }
        override suspend fun actualizar(agregado: Compra): Result<Unit> {
            compras[agregado.id.valor] = agregado
            return Result.success(Unit)
        }
        override suspend fun eliminar(id: CompraId): Result<Unit> {
            compras.remove(id.valor)
            return Result.success(Unit)
        }
        override suspend fun obtenerTodos(): List<Compra> = compras.values.toList()
    }

    private class FakeProductoRepository : IProductoRepository {
        val productos = mutableMapOf<Int, Producto>()
        override suspend fun siguienteId(): ProductoId = ProductoId(productos.size + 1)
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
        override suspend fun buscarPorCriterio(criterio: String): List<Producto> =
            productos.values.filter { it.nombre.contains(criterio, ignoreCase = true) }
    }

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var fakeCompraRepository: FakeCompraRepository
    private lateinit var fakeProductoRepository: FakeProductoRepository
    private lateinit var registrarCompraUseCase: RegistrarCompraUseCase
    private lateinit var cancelarCompraUseCase: CancelarCompraUseCase
    private lateinit var viewModel: CompraViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeCompraRepository = FakeCompraRepository()
        fakeProductoRepository = FakeProductoRepository()

        fakeProductoRepository.productos[1] = Producto(
            id = ProductoId(1),
            informacion = InformacionProducto("Leche Entera", "Lala", "Lácteos"),
            precioVenta = Dinero(28.0),
            inventario = Inventario(5),
            estado = EstadoProducto.ACTIVO
        )

        registrarCompraUseCase = RegistrarCompraUseCase(fakeCompraRepository, fakeProductoRepository)
        cancelarCompraUseCase = CancelarCompraUseCase(fakeCompraRepository)

        viewModel = CompraViewModel(registrarCompraUseCase, cancelarCompraUseCase, fakeCompraRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `inicializacion lista las compras existentes`() = testScope.runTest {
        fakeCompraRepository.compras[1] = Compra(
            id = CompraId(1),
            informacion = InformacionCompra("2026-09-03", EstadoCompra.REGISTRADA, Dinero(500.0))
        )

        viewModel.onAction(CompraAction.OnCargarCompras)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.compras.size)
        assertEquals(500.0, state.compras[0].informacion.total.monto, 0.01)
    }

    @Test
    fun `OnRegistrarCompra crea compra y reabastece existencias`() = testScope.runTest {
        val params = RegistrarCompraParams(
            fecha = "2026-09-03",
            listaCompra = listOf(
                ProductoCompraParams(productoId = 1, cantidad = 10, costoUnitario = 22.0)
            )
        )

        viewModel.onAction(CompraAction.OnRegistrarCompra(params))
        advanceUntilIdle()

        assertEquals("Compra registrada correctamente", viewModel.uiState.value.mensajeAlerta)
        assertEquals(1, fakeCompraRepository.compras.size)
        assertEquals(15, fakeProductoRepository.productos[1]?.inventario?.disponibles)
    }

    @Test
    fun `OnConfirmarCancelarCompra cancela la orden usando CancelarCompraUseCase`() = testScope.runTest {
        fakeCompraRepository.compras[1] = Compra(
            id = CompraId(1),
            informacion = InformacionCompra("2026-09-03", EstadoCompra.REGISTRADA, Dinero(100.0))
        )

        viewModel.onAction(CompraAction.OnSolicitarCancelarCompra(1))
        viewModel.onAction(CompraAction.OnConfirmarCancelarCompra)
        advanceUntilIdle()

        assertEquals("Compra cancelada correctamente", viewModel.uiState.value.mensajeAlerta)
        assertEquals(EstadoCompra.CANCELADA, fakeCompraRepository.compras[1]?.informacion?.estado)
    }
}
