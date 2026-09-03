package com.goodgus.localapplication.common.domain

/**
 * Value Object inmutable que representa cantidades monetarias.
 * Garantiza montos no negativos y encapsula operaciones aritméticas seguras.
 */
data class Dinero(val monto: Double) {
    init {
        require(monto >= 0.0) { "El monto no puede ser negativo: $monto" }
    }

    operator fun plus(otro: Dinero): Dinero = Dinero(this.monto + otro.monto)
    operator fun times(cantidad: Int): Dinero = Dinero(this.monto * cantidad)

    companion object {
        val CERO = Dinero(0.0)
    }
}
