package com.goodgus.localapplication.pedidos.domain.model

import com.goodgus.localapplication.common.domain.AggregateRoot
import com.goodgus.localapplication.common.domain.PedidoYaFinalizadoException

/**
 * Raíz de Agregado (Aggregate Root) del Bounded Context de Pedidos.
 * Gestiona el ciclo de vida, entregas, cancelaciones y actualizaciones de pedidos/encargos.
 */
data class Pedido(
    override val id: PedidoId,
    val informacion: InformacionPedido,
    val plazo: PlazoEntrega,
    val estado: EstadoPedido = EstadoPedido.PENDIENTE
) : AggregateRoot<PedidoId> {

    val estaPendiente: Boolean get() = estado == EstadoPedido.PENDIENTE

    /**
     * Regla de negocio: Marcar el pedido como entregado y registrar la fecha de entrega efectiva.
     */
    fun entregar(fechaEntregaEfectiva: String? = null): Result<Pedido> {
        if (!estaPendiente) {
            return Result.failure(PedidoYaFinalizadoException(id))
        }

        val plazoActualizado = if (fechaEntregaEfectiva != null) {
            plazo.copy(fechaEntrega = fechaEntregaEfectiva)
        } else {
            plazo
        }

        val pedidoEntregado = copy(
            estado = EstadoPedido.ENTREGADO,
            plazo = plazoActualizado
        )

        return Result.success(pedidoEntregado)
    }

    /**
     * Regla de negocio: Cancelar el pedido.
     */
    fun cancelar(): Result<Pedido> {
        if (!estaPendiente) {
            return Result.failure(PedidoYaFinalizadoException(id))
        }

        val pedidoCancelado = copy(estado = EstadoPedido.CANCELADO)
        return Result.success(pedidoCancelado)
    }

    /**
     * Regla de negocio: Actualizar los datos informativos y plazo del pedido.
     */
    fun actualizarInformacion(
        nuevaInformacion: InformacionPedido,
        nuevoPlazo: PlazoEntrega
    ): Result<Pedido> {
        if (!estaPendiente) {
            return Result.failure(PedidoYaFinalizadoException(id))
        }

        val pedidoActualizado = copy(
            informacion = nuevaInformacion,
            plazo = nuevoPlazo
        )

        return Result.success(pedidoActualizado)
    }
}
