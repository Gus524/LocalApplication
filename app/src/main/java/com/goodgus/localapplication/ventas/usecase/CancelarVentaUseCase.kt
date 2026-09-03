package com.goodgus.localapplication.ventas.usecase

import com.goodgus.localapplication.common.usecase.BaseUseCase
import com.goodgus.localapplication.ventas.domain.model.Cuenta
import com.goodgus.localapplication.ventas.domain.model.CuentaId
import com.goodgus.localapplication.ventas.domain.model.VentaId
import com.goodgus.localapplication.ventas.domain.repository.ICuentaRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

import javax.inject.Inject

data class CancelarVentaParams(
    val cuentaId: Int,
    val ventaId: Int
)

class CancelarVentaUseCase @Inject constructor(
    private val cuentaRepository: ICuentaRepository
) : BaseUseCase<CancelarVentaParams, Cuenta>() {

    override suspend fun ejecutar(params: CancelarVentaParams): Result<Cuenta> {
        val cuentaId = CuentaId(params.cuentaId)
        val cuenta = cuentaRepository.obtenerPorId(cuentaId)
            ?: return Result.failure(NoSuchElementException("Cuenta con ID ${params.cuentaId} no encontrada"))

        val resultadoCancelacion = cuenta.cancelarVenta(VentaId(params.ventaId))
        if (resultadoCancelacion.isFailure) {
            return Result.failure(resultadoCancelacion.exceptionOrNull()!!)
        }

        val cuentaActualizada = resultadoCancelacion.getOrThrow()
        val actualizarResult = cuentaRepository.actualizar(cuentaActualizada)
        return actualizarResult.map { cuentaActualizada }
    }
}
