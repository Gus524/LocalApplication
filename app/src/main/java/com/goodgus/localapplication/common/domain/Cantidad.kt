package com.goodgus.localapplication.common.domain

/**
 * Value Object inmutable que representa cantidades enteras en transacciones y movimientos.
 * Garantiza que el valor sea estrictamente positivo (> 0).
 */
@JvmInline
value class Cantidad(val valor: Int) {
    init {
        require(valor > 0) { "La cantidad debe ser estrictamente mayor a cero: $valor" }
    }
}
