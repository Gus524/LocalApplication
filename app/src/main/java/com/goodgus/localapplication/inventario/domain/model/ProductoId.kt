package com.goodgus.localapplication.inventario.domain.model

@JvmInline
value class ProductoId(val valor: Int) {
    init {
        require(valor >= 0) { "El identificador del producto no puede ser negativo: $valor" }
    }
}
