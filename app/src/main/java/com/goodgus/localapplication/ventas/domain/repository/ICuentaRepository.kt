package com.goodgus.localapplication.ventas.domain.repository

import com.goodgus.localapplication.core.domain.IRepository
import com.goodgus.localapplication.ventas.domain.model.Cuenta
import com.goodgus.localapplication.ventas.domain.model.CuentaId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Contrato de repositorio exclusivo para el Agregado Cuenta.
 */
interface ICuentaRepository : IRepository<Cuenta, CuentaId> {
    suspend fun obtenerCuentaActiva(): Cuenta?
    fun observarCuentaActiva(): Flow<Cuenta?> = flow {
        emit(obtenerCuentaActiva())
    }
}
