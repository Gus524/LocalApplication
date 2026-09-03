package com.goodgus.localapplication.pedidos.domain

import com.goodgus.localapplication.core.domain.PedidoYaFinalizadoException
import com.goodgus.localapplication.pedidos.domain.model.EstadoPedido
import com.goodgus.localapplication.pedidos.domain.model.InformacionPedido
import com.goodgus.localapplication.pedidos.domain.model.Pedido
import com.goodgus.localapplication.pedidos.domain.model.PedidoId
import com.goodgus.localapplication.pedidos.domain.model.PlazoEntrega
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class PedidoTest {

    private fun crearPedido(
        id: Int = 1,
        descripcion: String = "Pastel de Chocolate 3 leches",
        detalles: String? = "Con dedicatoria de feliz cumpleaños",
        fechaPedido: String = "2026-09-02",
        fechaEntrega: String? = "2026-09-05",
        estado: EstadoPedido = EstadoPedido.PENDIENTE
    ): Pedido {
        return Pedido(
            id = PedidoId(id),
            informacion = InformacionPedido(descripcion = descripcion, detalles = detalles),
            plazo = PlazoEntrega(fechaPedido = fechaPedido, fechaEntrega = fechaEntrega),
            estado = estado
        )
    }

    @Test
    fun `PedidoId rechaza valor negativo`() {
        assertThrows(IllegalArgumentException::class.java) {
            PedidoId(-1)
        }
    }

    @Test
    fun `InformacionPedido rechaza descripcion vacia`() {
        assertThrows(IllegalArgumentException::class.java) {
            InformacionPedido(descripcion = "   ")
        }
    }

    @Test
    fun `PlazoEntrega rechaza fechaPedido vacia`() {
        assertThrows(IllegalArgumentException::class.java) {
            PlazoEntrega(fechaPedido = "")
        }
    }

    @Test
    fun `entregar pedido pendiente actualiza estado a ENTREGADO`() {
        val pedido = crearPedido()
        val resultado = pedido.entregar(fechaEntregaEfectiva = "2026-09-04")

        assertTrue(resultado.isSuccess)
        val pedidoEntregado = resultado.getOrThrow()

        assertEquals(EstadoPedido.ENTREGADO, pedidoEntregado.estado)
        assertEquals("2026-09-04", pedidoEntregado.plazo.fechaEntrega)
        assertFalse(pedidoEntregado.estaPendiente)
    }

    @Test
    fun `entregar pedido ya entregado falla con PedidoYaFinalizadoException`() {
        val pedido = crearPedido(estado = EstadoPedido.ENTREGADO)
        val resultado = pedido.entregar()

        assertTrue(resultado.isFailure)
        assertTrue(resultado.exceptionOrNull() is PedidoYaFinalizadoException)
    }

    @Test
    fun `cancelar pedido pendiente actualiza estado a CANCELADO`() {
        val pedido = crearPedido()
        val resultado = pedido.cancelar()

        assertTrue(resultado.isSuccess)
        val pedidoCancelado = resultado.getOrThrow()

        assertEquals(EstadoPedido.CANCELADO, pedidoCancelado.estado)
        assertFalse(pedidoCancelado.estaPendiente)
    }

    @Test
    fun `cancelar pedido ya cancelado falla con PedidoYaFinalizadoException`() {
        val pedido = crearPedido(estado = EstadoPedido.CANCELADO)
        val resultado = pedido.cancelar()

        assertTrue(resultado.isFailure)
        assertTrue(resultado.exceptionOrNull() is PedidoYaFinalizadoException)
    }

    @Test
    fun `actualizar informacion de pedido pendiente actualiza datos correctamente`() {
        val pedido = crearPedido()
        val nuevaInfo = InformacionPedido("Pastel Red Velvet", "Sin nueces")
        val nuevoPlazo = PlazoEntrega("2026-09-02", "2026-09-06")

        val resultado = pedido.actualizarInformacion(nuevaInfo, nuevoPlazo)

        assertTrue(resultado.isSuccess)
        val pedidoActualizado = resultado.getOrThrow()

        assertEquals("Pastel Red Velvet", pedidoActualizado.informacion.descripcion)
        assertEquals("Sin nueces", pedidoActualizado.informacion.detalles)
        assertEquals("2026-09-06", pedidoActualizado.plazo.fechaEntrega)
    }

    @Test
    fun `actualizar informacion en pedido finalizado falla con PedidoYaFinalizadoException`() {
        val pedido = crearPedido(estado = EstadoPedido.ENTREGADO)
        val nuevaInfo = InformacionPedido("Pastel", null)
        val nuevoPlazo = PlazoEntrega("2026-09-02")

        val resultado = pedido.actualizarInformacion(nuevaInfo, nuevoPlazo)

        assertTrue(resultado.isFailure)
        assertTrue(resultado.exceptionOrNull() is PedidoYaFinalizadoException)
    }
}
