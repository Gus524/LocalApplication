package com.goodgus.localapplication.pedidos.usecase

import com.goodgus.localapplication.common.usecase.BaseUseCase
import com.goodgus.localapplication.pedidos.domain.model.InformacionPedido
import com.goodgus.localapplication.pedidos.domain.model.Pedido
import com.goodgus.localapplication.pedidos.domain.model.PedidoId
import com.goodgus.localapplication.pedidos.domain.model.PlazoEntrega
import com.goodgus.localapplication.pedidos.domain.repository.IPedidoRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

import javax.inject.Inject

data class EditarPedidoParams(
    val pedidoId: Int,
    val nuevaDescripcion: String,
    val nuevosDetalles: String? = null,
    val nuevaFechaEntrega: String? = null
)

class EditarPedidoUseCase @Inject constructor(
    private val pedidoRepository: IPedidoRepository
) : BaseUseCase<EditarPedidoParams, Pedido>() {

    override suspend fun ejecutar(params: EditarPedidoParams): Result<Pedido> {
        val pedidoId = PedidoId(params.pedidoId)
        val pedido = pedidoRepository.obtenerPorId(pedidoId)
            ?: return Result.failure(NoSuchElementException("Pedido con ID ${params.pedidoId} no encontrado"))

        val nuevaInformacion = InformacionPedido(
            descripcion = params.nuevaDescripcion,
            detalles = params.nuevosDetalles
        )
        val nuevoPlazo = PlazoEntrega(
            fechaPedido = pedido.plazo.fechaPedido,
            fechaEntrega = params.nuevaFechaEntrega ?: pedido.plazo.fechaEntrega
        )

        val resultadoActualizacion = pedido.actualizarInformacion(nuevaInformacion, nuevoPlazo)
        if (resultadoActualizacion.isFailure) {
            return Result.failure(resultadoActualizacion.exceptionOrNull()!!)
        }

        val pedidoActualizado = resultadoActualizacion.getOrThrow()
        val actualizarResult = pedidoRepository.actualizar(pedidoActualizado)
        return actualizarResult.map { pedidoActualizado }
    }
}
