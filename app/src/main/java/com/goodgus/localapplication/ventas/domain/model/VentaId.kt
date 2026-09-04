package com.goodgus.localapplication.ventas.domain.model

@JvmInline
value class VentaId(val valor: Int) {
    init {
        require(valor >= 0) { "El identificador de la venta no puede ser negativo: $valor" }
    }
}
