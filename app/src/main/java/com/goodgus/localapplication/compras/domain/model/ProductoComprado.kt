package com.goodgus.localapplication.compras.domain.model

import com.goodgus.localapplication.core.domain.Dinero
import com.goodgus.localapplication.core.domain.Entity
import com.goodgus.localapplication.inventario.domain.model.ProductoId

/**
 * Entidad interna del Agregado Compra.
 * Representa una línea individual de producto adquirido en el lote de compra.
 */
data class ProductoComprado(
    override val id: ProductoCompradoId,
    val productoId: ProductoId,
    val nombreProducto: String,
    val detalle: DetalleCompra
) : Entity<ProductoCompradoId> {

    val subtotal: Dinero get() = detalle.subtotal
}
