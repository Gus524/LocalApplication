package com.goodgus.localapplication.ventas.usecase

import com.goodgus.localapplication.common.usecase.BaseUseCase
import com.goodgus.localapplication.ventas.domain.model.Cuenta
import com.goodgus.localapplication.ventas.domain.model.CuentaId
import com.goodgus.localapplication.ventas.domain.repository.ICuentaRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

data class ConsultarCuentaParams(
    val id: Int? = null
)

class ConsultarCuentaUseCase(
    private val cuentaRepository: ICuentaRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default
) : BaseUseCase<ConsultarCuentaParams, Cuenta>(dispatcher) {

    override suspend fun ejecutar(params: ConsultarCuentaParams): Result<Cuenta> {
        val cuenta = if (params.id != null) {
            cuentaRepository.obtenerPorId(CuentaId(params.id))
        } else {
            cuentaRepository.obtenerCuentaActiva()
        }

        return if (cuenta != null) {
            Result.success(cuenta)
        } else {
            Result.failure(NoSuchElementException("No se encontró la cuenta solicitada"))
        }
    }
}
