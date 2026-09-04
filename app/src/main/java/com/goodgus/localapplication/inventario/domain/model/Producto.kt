package com.goodgus.localapplication.inventario.domain.model

import com.goodgus.localapplication.core.domain.AggregateRoot
import com.goodgus.localapplication.core.domain.Dinero
import com.goodgus.localapplication.core.domain.ProductoInactivoException
import com.goodgus.localapplication.core.domain.StockInsuficienteException

/**
 * Raíz de Agregado (Aggregate Root) del Bounded Context de Inventario.
 * Encapsula la consistencia transaccional del artículo, existencias y precios.
 */
data class Producto(
    override val id: ProductoId,
    val informacion: InformacionProducto,
    val precioVenta: Dinero,
    val inventario: Inventario,
    val estado: EstadoProducto = EstadoProducto.ACTIVO
) : AggregateRoot<ProductoId> {

    val nombre: String get() = informacion.nombre
    val marca: String get() = informacion.marca
    val tipo: String get() = informacion.tipo
    val estaActivo: Boolean get() = estado == EstadoProducto.ACTIVO

    /**
     * Regla de negocio: Descontar existencias físicas para una venta.
     */
    fun descontarStock(cantidad: Int): Result<Producto> {
        if (!estaActivo) {
            return Result.failure(ProductoInactivoException(id))
        }
        if (!inventario.tieneExistenciasPara(cantidad)) {
            return Result.failure(
                StockInsuficienteException(
                    productoId = id,
                    disponibles = inventario.disponibles,
                    requeridos = cantidad
                )
            )
        }
        return runCatching {
            copy(inventario = inventario.descontar(cantidad))
        }
    }

    /**
     * Regla de negocio: Reabastecimiento de existencias físicas.
     */
    fun reabastecer(cantidad: Int): Producto {
        return copy(inventario = inventario.reabastecer(cantidad))
    }

    /**
     * Regla de negocio: Actualización del precio de venta oficial.
     */
    fun actualizarPrecio(nuevoPrecio: Dinero): Producto {
        return copy(precioVenta = nuevoPrecio)
    }

    /**
     * Regla de negocio: Desactivación lógica del producto en catálogo.
     */
    fun desactivar(): Producto = copy(estado = EstadoProducto.INACTIVO)

    /**
     * Regla de negocio: Reactivación lógica del producto en catálogo.
     */
    fun activar(): Producto = copy(estado = EstadoProducto.ACTIVO)
}
