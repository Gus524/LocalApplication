package com.goodgus.localapplication.compras.domain.model

import com.goodgus.localapplication.common.domain.Cantidad
import com.goodgus.localapplication.common.domain.Dinero

/**
 * Value Object inmutable que modela el detalle de cantidad y costo de adquisición.
 */
data class DetalleCompra(
    val cantidad: Cantidad,
    val costoUnitario: Dinero
) {
    /**
     * Invariante de cálculo exacto del subtotal monetario de la compra.
     */
    val subtotal: Dinero get() = costoUnitario * cantidad.valor
}
