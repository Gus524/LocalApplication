package com.goodgus.localapplication.ventas.usecase

import com.goodgus.localapplication.common.usecase.BaseUseCase
import com.goodgus.localapplication.ventas.domain.model.Cuenta
import com.goodgus.localapplication.ventas.domain.model.CuentaId
import com.goodgus.localapplication.ventas.domain.repository.ICuentaRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

import javax.inject.Inject

data class CerrarCuentaParams(
    val cuentaId: Int
)

class CerrarCuentaUseCase @Inject constructor(
    private val cuentaRepository: ICuentaRepository
) : BaseUseCase<CerrarCuentaParams, Cuenta>() {

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
