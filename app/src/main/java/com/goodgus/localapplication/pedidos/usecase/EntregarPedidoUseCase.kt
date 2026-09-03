package com.goodgus.localapplication.pedidos.usecase

import com.goodgus.localapplication.common.usecase.BaseUseCase
import com.goodgus.localapplication.pedidos.domain.model.Pedido
import com.goodgus.localapplication.pedidos.domain.model.PedidoId
import com.goodgus.localapplication.pedidos.domain.repository.IPedidoRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

data class EntregarPedidoParams(
    val pedidoId: Int,
    val fechaEntregaEfectiva: String? = null
)

class EntregarPedidoUseCase(
    private val pedidoRepository: IPedidoRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default
) : BaseUseCase<EntregarPedidoParams, Pedido>(dispatcher) {

    override suspend fun ejecutar(params: EntregarPedidoParams): Result<Pedido> {
        val pedidoId = PedidoId(params.pedidoId)
        val pedido = pedidoRepository.obtenerPorId(pedidoId)
            ?: return Result.failure(NoSuchElementException("Pedido con ID ${params.pedidoId} no encontrado"))

        val resultadoEntrega = pedido.entregar(params.fechaEntregaEfectiva)
        if (resultadoEntrega.isFailure) {
            return Result.failure(resultadoEntrega.exceptionOrNull()!!)
        }

        val pedidoEntregado = resultadoEntrega.getOrThrow()
        val actualizarResult = pedidoRepository.actualizar(pedidoEntregado)
        return actualizarResult.map { pedidoEntregado }
    }
}
