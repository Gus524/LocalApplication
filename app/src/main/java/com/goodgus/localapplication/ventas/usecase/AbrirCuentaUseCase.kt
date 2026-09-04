package com.goodgus.localapplication.ventas.usecase

import com.goodgus.localapplication.core.usecase.BaseUseCase
import com.goodgus.localapplication.ventas.domain.model.Cuenta
import com.goodgus.localapplication.ventas.domain.model.EstadoCuenta
import com.goodgus.localapplication.ventas.domain.model.InformacionCuenta
import com.goodgus.localapplication.ventas.domain.repository.ICuentaRepository

import javax.inject.Inject

data class AbrirCuentaParams(
    val fecha: String
)

class AbrirCuentaUseCase @Inject constructor(
    private val cuentaRepository: ICuentaRepository
) : BaseUseCase<AbrirCuentaParams, Cuenta>() {

    override suspend fun ejecutar(params: AbrirCuentaParams): Result<Cuenta> {
        val cuentaActiva = cuentaRepository.obtenerCuentaActiva()
        if (cuentaActiva != null) {
            return Result.failure(IllegalStateException("Ya existe una cuenta abierta actualmente (ID ${cuentaActiva.id.valor})"))
        }

        val nuevoId = cuentaRepository.siguienteId()
        val nuevaCuenta = Cuenta(
            id = nuevoId,
            informacion = InformacionCuenta(
                fecha = params.fecha,
                estado = EstadoCuenta.ABIERTA
            )
        )

        val guardarResult = cuentaRepository.guardar(nuevaCuenta)
        return guardarResult.map { nuevaCuenta }
    }
}
