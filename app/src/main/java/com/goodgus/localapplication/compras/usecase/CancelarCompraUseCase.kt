package com.goodgus.localapplication.compras.usecase

import com.goodgus.localapplication.common.usecase.BaseUseCase
import com.goodgus.localapplication.compras.domain.model.Compra
import com.goodgus.localapplication.compras.domain.model.CompraId
import com.goodgus.localapplication.compras.domain.repository.ICompraRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

import javax.inject.Inject

data class CancelarCompraParams(
    val compraId: Int
)

class CancelarCompraUseCase @Inject constructor(
    private val compraRepository: ICompraRepository
) : BaseUseCase<CancelarCompraParams, Compra>() {

    override suspend fun ejecutar(params: CancelarCompraParams): Result<Compra> {
        val compraId = CompraId(params.compraId)
        val compra = compraRepository.obtenerPorId(compraId)
            ?: return Result.failure(NoSuchElementException("Compra con ID ${params.compraId} no encontrada"))

        val resultadoCancelacion = compra.cancelar()
        if (resultadoCancelacion.isFailure) {
            return Result.failure(resultadoCancelacion.exceptionOrNull()!!)
        }

        val compraCancelada = resultadoCancelacion.getOrThrow()
        val actualizarResult = compraRepository.actualizar(compraCancelada)
        return actualizarResult.map { compraCancelada }
    }
}
