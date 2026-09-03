package com.goodgus.localapplication.pedidos.domain.model

/**
 * Value Object inmutable que modela las fechas de solicitud y entrega de un pedido.
 */
data class PlazoEntrega(
    val fechaPedido: String,
    val fechaEntrega: String? = null
) {
    init {
        require(fechaPedido.isNotBlank()) { "La fecha del pedido no puede estar vacía" }
    }
}
