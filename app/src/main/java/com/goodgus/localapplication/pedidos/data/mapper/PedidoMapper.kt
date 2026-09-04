package com.goodgus.localapplication.pedidos.data.mapper

import com.goodgus.localapplication.core.data.mapper.IMapper
import com.goodgus.localapplication.pedidos.data.repository.Pedidos as PedidosEntity
import com.goodgus.localapplication.pedidos.domain.model.EstadoPedido
import com.goodgus.localapplication.pedidos.domain.model.InformacionPedido
import com.goodgus.localapplication.pedidos.domain.model.Pedido
import com.goodgus.localapplication.pedidos.domain.model.PedidoId
import com.goodgus.localapplication.pedidos.domain.model.PlazoEntrega

class PedidoMapper : IMapper<Pedido, PedidosEntity> {
    override fun toDomain(entity: PedidosEntity): Pedido {
        val estado = when {
            entity.fechaEntrega == "CANCELADO" -> EstadoPedido.CANCELADO
            entity.fechaEntrega != null -> EstadoPedido.ENTREGADO
            else -> EstadoPedido.PENDIENTE
        }
        return Pedido(
            id = PedidoId(entity.idPedido),
            informacion = InformacionPedido(
                descripcion = entity.descripcion,
                detalles = entity.detalles
            ),
            plazo = PlazoEntrega(
                fechaPedido = entity.fechaPedido,
                fechaEntrega = entity.fechaEntrega
            ),
            estado = estado
        )
    }

    override fun toPersistence(domain: Pedido): PedidosEntity {
        val fechaEntrega = when (domain.estado) {
            EstadoPedido.CANCELADO -> "CANCELADO"
            EstadoPedido.ENTREGADO -> domain.plazo.fechaEntrega ?: "ENTREGADO"
            EstadoPedido.PENDIENTE -> domain.plazo.fechaEntrega
        }
        return PedidosEntity(
            idPedido = domain.id.valor,
            fechaPedido = domain.plazo.fechaPedido,
            fechaEntrega = fechaEntrega,
            descripcion = domain.informacion.descripcion,
            detalles = domain.informacion.detalles
        )
    }
}
