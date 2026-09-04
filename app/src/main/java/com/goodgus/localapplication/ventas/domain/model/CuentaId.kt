package com.goodgus.localapplication.ventas.domain.model

@JvmInline
value class CuentaId(val valor: Int) {
    init {
        require(valor >= 0) { "El identificador de la cuenta no puede ser negativo: $valor" }
    }
}
