package com.goodgus.localapplication.compras.domain.model

/**
 * Value Object identificador fuertemente tipado del Agregado Compra.
 */
@JvmInline
value class CompraId(val valor: Int) {
    init {
        require(valor >= 0) { "El identificador de la compra no puede ser negativo: $valor" }
    }
}
