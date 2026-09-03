package com.goodgus.localapplication.compras.usecase

import com.goodgus.localapplication.common.usecase.BaseUseCase
import com.goodgus.localapplication.compras.domain.model.Compra
import com.goodgus.localapplication.compras.domain.model.CompraId
import com.goodgus.localapplication.compras.domain.repository.ICompraRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

data class ConsultarCompraParams(
    val id: Int
)

class ConsultarCompraUseCase(
    private val compraRepository: ICompraRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default
) : BaseUseCase<ConsultarCompraParams, Compra>(dispatcher) {

    override suspend fun ejecutar(params: ConsultarCompraParams): Result<Compra> {
        val compraId = CompraId(params.id)
        val compra = compraRepository.obtenerPorId(compraId)
            ?: return Result.failure(NoSuchElementException("Compra con ID ${params.id} no encontrada"))

        return Result.success(compra)
    }
}
