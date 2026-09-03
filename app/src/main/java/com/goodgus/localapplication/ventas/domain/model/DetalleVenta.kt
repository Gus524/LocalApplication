package com.goodgus.localapplication.ventas.domain.model

import com.goodgus.localapplication.common.domain.Cantidad
import com.goodgus.localapplication.common.domain.Dinero

/**
 * Value Object que encapsula los detalles de la transacción de venta.
 */
data class DetalleVenta(
    val cantidad: Cantidad,
    val hora: String,
    val precioUnitario: Dinero
) {
    /**
     * Invariante de cálculo exacto del subtotal monetario.
     */
    val subtotal: Dinero get() = precioUnitario * cantidad.valor
}
