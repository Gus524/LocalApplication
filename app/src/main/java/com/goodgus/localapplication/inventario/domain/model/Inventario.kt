package com.goodgus.localapplication.inventario.domain.model

/**
 * Value Object inmutable que modela las existencias físicas disponibles de un producto.
 */
@JvmInline
value class Inventario(val disponibles: Int) {
    init {
        require(disponibles >= 0) { "Las existencias disponibles no pueden ser negativas: $disponibles" }
    }

    fun tieneExistenciasPara(cantidadRequerida: Int): Boolean = this.disponibles >= cantidadRequerida

    fun descontar(cantidad: Int): Inventario {
        require(cantidad > 0) { "La cantidad a descontar debe ser mayor a cero: $cantidad" }
        require(tieneExistenciasPara(cantidad)) {
            "Existencias insuficientes. Disponibles: $disponibles, Requeridas: $cantidad"
        }
        return Inventario(this.disponibles - cantidad)
    }

    fun reabastecer(cantidad: Int): Inventario {
        require(cantidad > 0) { "La cantidad a reabastecer debe ser mayor a cero: $cantidad" }
        return Inventario(this.disponibles + cantidad)
    }
}
