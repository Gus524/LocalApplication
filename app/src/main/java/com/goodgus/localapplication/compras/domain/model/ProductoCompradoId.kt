package com.goodgus.localapplication.compras.domain.model

/**
 * Value Object identificador fuertemente tipado de la Entidad ProductoComprado.
 */
@JvmInline
value class ProductoCompradoId(val valor: Int) {
    init {
        require(valor >= 0) { "El identificador del producto comprado no puede ser negativo: $valor" }
    }
}
