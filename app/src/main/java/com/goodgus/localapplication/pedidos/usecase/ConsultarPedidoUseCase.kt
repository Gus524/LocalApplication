package com.goodgus.localapplication.pedidos.usecase

import com.goodgus.localapplication.common.usecase.BaseUseCase
import com.goodgus.localapplication.pedidos.domain.model.Pedido
import com.goodgus.localapplication.pedidos.domain.model.PedidoId
import com.goodgus.localapplication.pedidos.domain.repository.IPedidoRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

data class ConsultarPedidoParams(
    val id: Int
)

class ConsultarPedidoUseCase(
    private val pedidoRepository: IPedidoRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default
) : BaseUseCase<ConsultarPedidoParams, Pedido>(dispatcher) {

    override suspend fun ejecutar(params: ConsultarPedidoParams): Result<Pedido> {
        val pedidoId = PedidoId(params.id)
        val pedido = pedidoRepository.obtenerPorId(pedidoId)
            ?: return Result.failure(NoSuchElementException("Pedido con ID ${params.id} no encontrado"))

        return Result.success(pedido)
    }
}
