package com.goodgus.localapplication.viewModels

import android.app.Application
import com.goodgus.localapplication.pedidos.domain.model.EstadoPedido
import com.goodgus.localapplication.pedidos.domain.model.InformacionPedido
import com.goodgus.localapplication.pedidos.domain.model.Pedido
import com.goodgus.localapplication.pedidos.domain.model.PedidoId
import com.goodgus.localapplication.pedidos.domain.model.PlazoEntrega
import com.goodgus.localapplication.pedidos.domain.repository.IPedidoRepository
import com.goodgus.localapplication.pedidos.ui.viewModels.PedidosViewModel
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
class PedidosViewModelTest {

    private class TestApp : Application()

    private class FakePedidoRepository : IPedidoRepository {
        val pedidos = mutableMapOf<Int, Pedido>()
        private var idCounter = 1

        override suspend fun siguienteId(): PedidoId = PedidoId(idCounter++)
        override suspend fun obtenerPorId(id: PedidoId): Pedido? = pedidos[id.valor]
        override suspend fun guardar(agregado: Pedido): Result<Unit> {
            pedidos[agregado.id.valor] = agregado
            return Result.success(Unit)
        }
        override suspend fun actualizar(agregado: Pedido): Result<Unit> {
            pedidos[agregado.id.valor] = agregado
            return Result.success(Unit)
        }
        override suspend fun eliminar(id: PedidoId): Result<Unit> {
            pedidos.remove(id.valor)
            return Result.success(Unit)
        }
        override suspend fun obtenerTodos(): List<Pedido> = pedidos.values.toList()
    }

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var fakeRepository: FakePedidoRepository
    private lateinit var viewModel: PedidosViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakePedidoRepository()

        fakeRepository.pedidos[1] = Pedido(
            id = PedidoId(1),
            informacion = InformacionPedido("Pastel de chocolate", "Para 20 personas"),
            plazo = PlazoEntrega("2026-09-03", "2026-09-05"),
            estado = EstadoPedido.PENDIENTE
        )

        viewModel = PedidosViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `cargarPedidos carga pedidos correctamente en uiState y StateFlow`() = testScope.runTest {
        viewModel.cargarPedidos()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.pedidos.size)
        assertEquals("Pastel de chocolate", state.pedidos[0].descripcion)
        assertEquals("Para 20 personas", state.pedidos[0].detalles)
        assertFalse(state.isBusy)
        assertEquals(1, viewModel.pedidos.value.size)
    }
}
