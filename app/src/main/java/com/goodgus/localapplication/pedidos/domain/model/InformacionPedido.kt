package com.goodgus.localapplication.pedidos.domain.model

/**
 * Value Object inmutable que modela la descripción y detalles de un pedido.
 */
data class InformacionPedido(
    val descripcion: String,
    val detalles: String? = null
) {
    init {
        require(descripcion.isNotBlank()) { "La descripción del pedido no puede estar vacía" }
    }
}
