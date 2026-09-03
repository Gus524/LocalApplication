package com.goodgus.localapplication.ventas.domain.model

@JvmInline
value class CantidadVenta(val valor: Int) {
    init {
        require(valor > 0) { "La cantidad vendida debe ser al menos 1 unidad: $valor" }
    }
}
