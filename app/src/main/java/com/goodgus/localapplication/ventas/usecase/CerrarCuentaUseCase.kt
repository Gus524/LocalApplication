package com.goodgus.localapplication.ventas.usecase

import com.goodgus.localapplication.common.usecase.BaseUseCase
import com.goodgus.localapplication.ventas.domain.model.Cuenta
import com.goodgus.localapplication.ventas.domain.model.CuentaId
import com.goodgus.localapplication.ventas.domain.repository.ICuentaRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

data class CerrarCuentaParams(
    val cuentaId: Int
)

class CerrarCuentaUseCase(
    private val cuentaRepository: ICuentaRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default
) : BaseUseCase<CerrarCuentaParams, Cuenta>(dispatcher) {

    override suspend fun ejecutar(params: CerrarCuentaParams): Result<Cuenta> {
        val cuentaId = CuentaId(params.cuentaId)
        val cuenta = cuentaRepository.obtenerPorId(cuentaId)
            ?: return Result.failure(NoSuchElementException("Cuenta con ID ${params.cuentaId} no encontrada"))

        val resultadoCierre = cuenta.cerrar()
        if (resultadoCierre.isFailure) {
            return Result.failure(resultadoCierre.exceptionOrNull()!!)
        }

        val cuentaCerrada = resultadoCierre.getOrThrow()
        val actualizarResult = cuentaRepository.actualizar(cuentaCerrada)
        return actualizarResult.map { cuentaCerrada }
    }
}
