package com.goodgus.localapplication.compras.domain.model

import com.goodgus.localapplication.core.domain.Dinero

/**
 * Value Object inmutable que agrupa los metadatos generales de una compra.
 */
data class InformacionCompra(
    val fechaCompra: String,
    val estado: EstadoCompra = EstadoCompra.REGISTRADA,
    val total: Dinero = Dinero.CERO
) {
    init {
        require(fechaCompra.isNotBlank()) { "La fecha de compra no puede estar vacía" }
    }
}
