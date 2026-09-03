package com.goodgus.localapplication.compras.domain.model

import com.goodgus.localapplication.core.domain.AggregateRoot
import com.goodgus.localapplication.core.domain.Cantidad
import com.goodgus.localapplication.core.domain.CompraYaFinalizadaException
import com.goodgus.localapplication.core.domain.Dinero
import com.goodgus.localapplication.core.domain.ProductoInactivoException
import com.goodgus.localapplication.inventario.domain.model.Producto

/**
 * Raíz de Agregado (Aggregate Root) del Bounded Context de Compras.
 * Gestiona la orden de abastecimiento, el reabastecimiento atómico de stock y los balances de compra.
 */
data class Compra(
    override val id: CompraId,
    val informacion: InformacionCompra,
    val productos: List<ProductoComprado> = emptyList()
) : AggregateRoot<CompraId> {

    val estaRegistrada: Boolean get() = informacion.estado == EstadoCompra.REGISTRADA

    val totalCalculado: Dinero
        get() = productos.fold(Dinero.CERO) { acc, p -> acc + p.subtotal }

    /**
     * Regla de negocio: Agregar un producto comprado a la orden y reabastecer el stock del producto.
     */
    fun agregarProducto(
        lineaId: ProductoCompradoId,
        producto: Producto,
        cantidad: Cantidad,
        costoUnitario: Dinero
    ): Result<Pair<Compra, Producto>> {
        if (!estaRegistrada) {
            return Result.failure(CompraYaFinalizadaException(id))
        }

        if (!producto.estaActivo) {
            return Result.failure(ProductoInactivoException(producto.id))
        }

        val productoReabastecido = producto.reabastecer(cantidad.valor)

        val nuevoProductoComprado = ProductoComprado(
            id = lineaId,
            productoId = producto.id,
            nombreProducto = producto.nombre,
            detalle = DetalleCompra(
                cantidad = cantidad,
                costoUnitario = costoUnitario
            )
        )

        val nuevosProductos = productos + nuevoProductoComprado
        val nuevoTotal = recalcularTotal(nuevosProductos)

        val compraActualizada = copy(
            informacion = informacion.copy(total = nuevoTotal),
            productos = nuevosProductos
        )

        return Result.success(Pair(compraActualizada, productoReabastecido))
    }

    /**
     * Regla de negocio: Cancelar la orden de compra.
     */
    fun cancelar(): Result<Compra> {
        if (!estaRegistrada) {
            return Result.failure(CompraYaFinalizadaException(id))
        }

        val compraCancelada = copy(
            informacion = informacion.copy(estado = EstadoCompra.CANCELADA)
        )

        return Result.success(compraCancelada)
    }

    private fun recalcularTotal(lista: List<ProductoComprado>): Dinero {
        return lista.fold(Dinero.CERO) { acc, p -> acc + p.subtotal }
    }
}
