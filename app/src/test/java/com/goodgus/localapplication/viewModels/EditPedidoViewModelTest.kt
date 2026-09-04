package com.goodgus.localapplication.viewModels

import com.goodgus.localapplication.pedidos.domain.model.EstadoPedido
import com.goodgus.localapplication.pedidos.domain.model.InformacionPedido
import com.goodgus.localapplication.pedidos.domain.model.Pedido
import com.goodgus.localapplication.pedidos.domain.model.PedidoId
import com.goodgus.localapplication.pedidos.domain.model.PlazoEntrega
import com.goodgus.localapplication.pedidos.domain.repository.IPedidoRepository
import com.goodgus.localapplication.pedidos.ui.viewModels.EditPedidoAction
import com.goodgus.localapplication.pedidos.ui.viewModels.EditPedidoEffect
import com.goodgus.localapplication.pedidos.ui.viewModels.EditPedidoViewModel
import com.goodgus.localapplication.pedidos.usecase.CrearPedidoUseCase
import com.goodgus.localapplication.pedidos.usecase.EditarPedidoUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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
class EditPedidoViewModelTest {

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
    private lateinit var crearPedidoUseCase: CrearPedidoUseCase
    private lateinit var editarPedidoUseCase: EditarPedidoUseCase
    private lateinit var viewModel: EditPedidoViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakePedidoRepository()
        crearPedidoUseCase = CrearPedidoUseCase(fakeRepository)
        editarPedidoUseCase = EditarPedidoUseCase(fakeRepository)
        viewModel = EditPedidoViewModel(crearPedidoUseCase, editarPedidoUseCase, fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `OnGuardar crea un nuevo pedido y emite efecto NavegarAtras`() = testScope.runTest {
        viewModel.onAction(EditPedidoAction.OnDescripcionChange("Caja de donas surtidas"))
        viewModel.onAction(EditPedidoAction.OnDetallesChange("6 de chocolate y 6 de glaseadas"))

        viewModel.onAction(EditPedidoAction.OnGuardar)
        advanceUntilIdle()

        assertEquals(1, fakeRepository.pedidos.size)
        val pedido = fakeRepository.pedidos[1]
        assertEquals("Caja de donas surtidas", pedido?.informacion?.descripcion)
        assertEquals("6 de chocolate y 6 de glaseadas", pedido?.informacion?.detalles)
        val effectReceived = viewModel.effect.first()
        assertEquals(EditPedidoEffect.NavegarAtras, effectReceived)
    }

    @Test
    fun `OnCargarPedido recupera informacion del pedido existente`() = testScope.runTest {
        fakeRepository.pedidos[5] = Pedido(
            id = PedidoId(5),
            informacion = InformacionPedido("Gelatina floral", "3 leches"),
            plazo = PlazoEntrega("2026-09-01"),
            estado = EstadoPedido.PENDIENTE
        )

        viewModel.onAction(EditPedidoAction.OnCargarPedido(5))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Gelatina floral", state.descripcion)
        assertEquals("3 leches", state.detalles)
        assertTrue(state.esEdicion)
    }
}
