package com.goodgus.localapplication.pedidos.usecase

import com.goodgus.localapplication.common.usecase.BaseUseCase
import com.goodgus.localapplication.pedidos.domain.model.EstadoPedido
import com.goodgus.localapplication.pedidos.domain.model.InformacionPedido
import com.goodgus.localapplication.pedidos.domain.model.Pedido
import com.goodgus.localapplication.pedidos.domain.model.PlazoEntrega
import com.goodgus.localapplication.pedidos.domain.repository.IPedidoRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

data class CrearPedidoParams(
    val descripcion: String,
    val detalles: String? = null,
    val fechaPedido: String,
    val fechaEntrega: String? = null
)

class CrearPedidoUseCase(
    private val pedidoRepository: IPedidoRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default
) : BaseUseCase<CrearPedidoParams, Pedido>(dispatcher) {

    override suspend fun ejecutar(params: CrearPedidoParams): Result<Pedido> {
        val nuevoPedidoId = pedidoRepository.siguienteId()

        val pedido = Pedido(
            id = nuevoPedidoId,
            informacion = InformacionPedido(
                descripcion = params.descripcion,
                detalles = params.detalles
            ),
            plazo = PlazoEntrega(
                fechaPedido = params.fechaPedido,
                fechaEntrega = params.fechaEntrega
            ),
            estado = EstadoPedido.PENDIENTE
        )

        val guardarResult = pedidoRepository.guardar(pedido)
        return guardarResult.map { pedido }
    }
}
