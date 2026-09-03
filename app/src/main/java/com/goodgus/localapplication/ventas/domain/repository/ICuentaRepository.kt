package com.goodgus.localapplication.ventas.domain.repository

import com.goodgus.localapplication.common.domain.IRepository
import com.goodgus.localapplication.ventas.domain.model.Cuenta
import com.goodgus.localapplication.ventas.domain.model.CuentaId

/**
 * Contrato de repositorio exclusivo para el Agregado Cuenta.
 */
interface ICuentaRepository : IRepository<Cuenta, CuentaId> {
    suspend fun obtenerCuentaActiva(): Cuenta?
}
