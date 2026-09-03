package com.goodgus.localapplication.ventas.domain.model

import com.goodgus.localapplication.core.domain.Dinero

/**
 * Value Object que actúa como snapshot del encabezado y balance acumulado de la cuenta.
 */
data class InformacionCuenta(
    val fecha: String,
    val estado: EstadoCuenta = EstadoCuenta.ABIERTA,
    val total: Dinero = Dinero.CERO
) {
    init {
        require(fecha.isNotBlank()) { "La fecha de la cuenta no puede estar vacía" }
    }

    val estaAbierta: Boolean get() = estado == EstadoCuenta.ABIERTA
}
