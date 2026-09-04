package com.goodgus.localapplication.ventas.usecase

import com.goodgus.localapplication.core.usecase.BaseUseCase
import com.goodgus.localapplication.ventas.domain.model.Cuenta
import com.goodgus.localapplication.ventas.domain.model.CuentaId
import com.goodgus.localapplication.ventas.domain.repository.ICuentaRepository

import javax.inject.Inject

data class ConsultarCuentaParams(
    val id: Int? = null
)

class ConsultarCuentaUseCase @Inject constructor(
    private val cuentaRepository: ICuentaRepository
) : BaseUseCase<ConsultarCuentaParams, Cuenta>() {

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
