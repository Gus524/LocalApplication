package com.goodgus.localapplication.pedidos.domain.model

/**
 * Value Object identificador fuertemente tipado del Agregado Pedido.
 */
@JvmInline
value class PedidoId(val valor: Int) {
    init {
        require(valor >= 0) { "El identificador del pedido no puede ser negativo: $valor" }
    }
}
