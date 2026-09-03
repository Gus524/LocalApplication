package com.goodgus.localapplication.pedidos.usecase

import com.goodgus.localapplication.common.usecase.BaseUseCase
import com.goodgus.localapplication.pedidos.domain.model.Pedido
import com.goodgus.localapplication.pedidos.domain.model.PedidoId
import com.goodgus.localapplication.pedidos.domain.repository.IPedidoRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

data class CancelarPedidoParams(
    val pedidoId: Int
)

class CancelarPedidoUseCase(
    private val pedidoRepository: IPedidoRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default
) : BaseUseCase<CancelarPedidoParams, Pedido>(dispatcher) {

    override suspend fun ejecutar(params: CancelarPedidoParams): Result<Pedido> {
        val pedidoId = PedidoId(params.pedidoId)
        val pedido = pedidoRepository.obtenerPorId(pedidoId)
            ?: return Result.failure(NoSuchElementException("Pedido con ID ${params.pedidoId} no encontrado"))

        val resultadoCancelacion = pedido.cancelar()
        if (resultadoCancelacion.isFailure) {
            return Result.failure(resultadoCancelacion.exceptionOrNull()!!)
        }

        val pedidoCancelado = resultadoCancelacion.getOrThrow()
        val actualizarResult = pedidoRepository.actualizar(pedidoCancelado)
        return actualizarResult.map { pedidoCancelado }
    }
}
