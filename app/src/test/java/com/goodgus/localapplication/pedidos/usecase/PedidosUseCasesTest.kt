package com.goodgus.localapplication.pedidos.usecase

import com.goodgus.localapplication.common.domain.PedidoYaFinalizadoException
import com.goodgus.localapplication.pedidos.domain.model.EstadoPedido
import com.goodgus.localapplication.pedidos.domain.model.Pedido
import com.goodgus.localapplication.pedidos.domain.model.PedidoId
import com.goodgus.localapplication.pedidos.domain.repository.IPedidoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PedidosUseCasesTest {

    private class FakePedidoRepository : IPedidoRepository {
        val pedidos = mutableMapOf<PedidoId, Pedido>()
        private var idCounter = 1

        override suspend fun siguienteId(): PedidoId = PedidoId(idCounter++)

        override suspend fun obtenerPorId(id: PedidoId): Pedido? = pedidos[id]

        override suspend fun guardar(agregado: Pedido): Result<Unit> {
            pedidos[agregado.id] = agregado
            return Result.success(Unit)
        }

        override suspend fun actualizar(agregado: Pedido): Result<Unit> {
            pedidos[agregado.id] = agregado
            return Result.success(Unit)
        }

        override suspend fun eliminar(id: PedidoId): Result<Unit> {
            pedidos.remove(id)
            return Result.success(Unit)
        }

        override suspend fun obtenerTodos(): List<Pedido> = pedidos.values.toList()
    }

    private lateinit var pedidoRepository: FakePedidoRepository
    private val testDispatcher = Dispatchers.Unconfined

    private lateinit var crearPedidoUseCase: CrearPedidoUseCase
    private lateinit var entregarPedidoUseCase: EntregarPedidoUseCase
    private lateinit var cancelarPedidoUseCase: CancelarPedidoUseCase
    private lateinit var editarPedidoUseCase: EditarPedidoUseCase
    private lateinit var consultarPedidoUseCase: ConsultarPedidoUseCase

    @Before
    fun setup() {
        pedidoRepository = FakePedidoRepository()
        crearPedidoUseCase = CrearPedidoUseCase(pedidoRepository, testDispatcher)
        entregarPedidoUseCase = EntregarPedidoUseCase(pedidoRepository, testDispatcher)
        cancelarPedidoUseCase = CancelarPedidoUseCase(pedidoRepository, testDispatcher)
        editarPedidoUseCase = EditarPedidoUseCase(pedidoRepository, testDispatcher)
        consultarPedidoUseCase = ConsultarPedidoUseCase(pedidoRepository, testDispatcher)
    }

    @Test
    fun `CrearPedidoUseCase registra nuevo pedido pendiente`() = runBlocking {
        val params = CrearPedidoParams(
            descripcion = "Pastel de Chocolate",
            detalles = "Dedicatoria especial",
            fechaPedido = "2026-09-02",
            fechaEntrega = "2026-09-05"
        )

        val resultado = crearPedidoUseCase(params)

        assertTrue(resultado.isSuccess)
        val pedido = resultado.getOrThrow()
        assertEquals(1, pedido.id.valor)
        assertEquals("Pastel de Chocolate", pedido.informacion.descripcion)
        assertEquals(EstadoPedido.PENDIENTE, pedido.estado)
        assertTrue(pedido.estaPendiente)
    }

    @Test
    fun `CrearPedidoUseCase rechaza descripcion vacia`() = runBlocking {
        val params = CrearPedidoParams(
            descripcion = "   ",
            fechaPedido = "2026-09-02"
        )

        val resultado = crearPedidoUseCase(params)

        assertTrue(resultado.isFailure)
        assertTrue(resultado.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `EntregarPedidoUseCase marca pedido como ENTREGADO y registra fecha`() = runBlocking {
        val pedido = crearPedidoUseCase(
            CrearPedidoParams(descripcion = "Mesa de dulces", fechaPedido = "2026-09-02")
        ).getOrThrow()

        val resultadoEntrega = entregarPedidoUseCase(
            EntregarPedidoParams(pedidoId = pedido.id.valor, fechaEntregaEfectiva = "2026-09-03")
        )

        assertTrue(resultadoEntrega.isSuccess)
        val pedidoEntregado = resultadoEntrega.getOrThrow()
        assertEquals(EstadoPedido.ENTREGADO, pedidoEntregado.estado)
        assertEquals("2026-09-03", pedidoEntregado.plazo.fechaEntrega)
        assertFalse(pedidoEntregado.estaPendiente)
    }

    @Test
    fun `EntregarPedidoUseCase falla si pedido ya esta entregado o cancelado`() = runBlocking {
        val pedido = crearPedidoUseCase(
            CrearPedidoParams(descripcion = "Arreglo", fechaPedido = "2026-09-02")
        ).getOrThrow()

        entregarPedidoUseCase(EntregarPedidoParams(pedidoId = pedido.id.valor))

        val segundaEntrega = entregarPedidoUseCase(EntregarPedidoParams(pedidoId = pedido.id.valor))

        assertTrue(segundaEntrega.isFailure)
        assertTrue(segundaEntrega.exceptionOrNull() is PedidoYaFinalizadoException)
    }

    @Test
    fun `CancelarPedidoUseCase cancela pedido pendiente`() = runBlocking {
        val pedido = crearPedidoUseCase(
            CrearPedidoParams(descripcion = "Gelatina decorada", fechaPedido = "2026-09-02")
        ).getOrThrow()

        val resultadoCancelacion = cancelarPedidoUseCase(CancelarPedidoParams(pedidoId = pedido.id.valor))

        assertTrue(resultadoCancelacion.isSuccess)
        val pedidoCancelado = resultadoCancelacion.getOrThrow()
        assertEquals(EstadoPedido.CANCELADO, pedidoCancelado.estado)
        assertFalse(pedidoCancelado.estaPendiente)
    }

    @Test
    fun `EditarPedidoUseCase actualiza descripcion y fecha de entrega`() = runBlocking {
        val pedido = crearPedidoUseCase(
            CrearPedidoParams(descripcion = "Cupcakes x12", fechaPedido = "2026-09-02", fechaEntrega = "2026-09-04")
        ).getOrThrow()

        val resultadoEdicion = editarPedidoUseCase(
            EditarPedidoParams(
                pedidoId = pedido.id.valor,
                nuevaDescripcion = "Cupcakes x24",
                nuevosDetalles = "Vainilla y chocolate",
                nuevaFechaEntrega = "2026-09-06"
            )
        )

        assertTrue(resultadoEdicion.isSuccess)
        val pedidoEditado = resultadoEdicion.getOrThrow()
        assertEquals("Cupcakes x24", pedidoEditado.informacion.descripcion)
        assertEquals("Vainilla y chocolate", pedidoEditado.informacion.detalles)
        assertEquals("2026-09-06", pedidoEditado.plazo.fechaEntrega)
    }

    @Test
    fun `ConsultarPedidoUseCase retorna pedido existente`() = runBlocking {
        val pedido = crearPedidoUseCase(
            CrearPedidoParams(descripcion = "Donas decoradas", fechaPedido = "2026-09-02")
        ).getOrThrow()

        val resultado = consultarPedidoUseCase(ConsultarPedidoParams(id = pedido.id.valor))

        assertTrue(resultado.isSuccess)
        assertEquals(pedido.id.valor, resultado.getOrThrow().id.valor)
    }

    @Test
    fun `ConsultarPedidoUseCase falla si no existe`() = runBlocking {
        val resultado = consultarPedidoUseCase(ConsultarPedidoParams(id = 999))

        assertTrue(resultado.isFailure)
        assertTrue(resultado.exceptionOrNull() is NoSuchElementException)
    }
}
