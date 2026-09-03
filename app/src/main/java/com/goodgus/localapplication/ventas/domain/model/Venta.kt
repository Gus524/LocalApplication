package com.goodgus.localapplication.ventas.domain.model

import com.goodgus.localapplication.common.domain.Dinero
import com.goodgus.localapplication.common.domain.Entity
import com.goodgus.localapplication.inventario.domain.model.ProductoId

/**
 * Entidad interna del Agregado Cuenta.
 * Representa una línea individual de producto vendida.
 */
data class Venta(
    override val id: VentaId,
    val productoId: ProductoId,
    val nombreProducto: String,
    val detalle: DetalleVenta,
    val estado: EstadoVenta = EstadoVenta.ACTIVA
) : Entity<VentaId> {

    val subtotal: Dinero
        get() = if (estado == EstadoVenta.ACTIVA) {
            detalle.subtotal
        } else {
            Dinero.CERO
        }

    fun cancelar(): Venta = copy(estado = EstadoVenta.CANCELADA)
}
