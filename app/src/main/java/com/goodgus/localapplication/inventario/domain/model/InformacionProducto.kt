package com.goodgus.localapplication.inventario.domain.model

/**
 * Value Object que encapsula los datos descriptivos del catálogo del producto.
 */
data class InformacionProducto(
    val nombre: String,
    val marca: String,
    val tipo: String
) {
    init {
        require(nombre.isNotBlank()) { "El nombre del producto no puede estar vacío" }
    }

    val nombreNormalizado: String get() = nombre.trim()
    val marcaNormalizada: String get() = marca.trim()
    val tipoNormalizado: String get() = tipo.trim()
}
