package com.goodgus.localapplication.compras.data.mapper

import com.goodgus.localapplication.core.data.mapper.IMapper
import com.goodgus.localapplication.core.domain.Cantidad
import com.goodgus.localapplication.core.domain.Dinero
import com.goodgus.localapplication.compras.domain.model.Compra
import com.goodgus.localapplication.compras.domain.model.CompraId
import com.goodgus.localapplication.compras.domain.model.DetalleCompra
import com.goodgus.localapplication.compras.domain.model.EstadoCompra
import com.goodgus.localapplication.compras.domain.model.InformacionCompra
import com.goodgus.localapplication.compras.domain.model.ProductoComprado
import com.goodgus.localapplication.compras.domain.model.ProductoCompradoId
import com.goodgus.localapplication.inventario.domain.model.ProductoId
import com.goodgus.localapplication.models.data.Compra as CompraEntity
import com.goodgus.localapplication.models.data.CompraProducto as CompraProductoEntity

class CompraMapper : IMapper<Compra, CompraEntity> {
    override fun toDomain(entity: CompraEntity): Compra {
        return toDomain(entity, emptyList(), emptyMap())
    }

    fun toDomain(
        entity: CompraEntity,
        items: List<CompraProductoEntity>,
        nombresProductos: Map<Int, String> = emptyMap()
    ): Compra {
        val productosComprados = items.map { item ->
            ProductoComprado(
                id = ProductoCompradoId(item.idCompraProducto),
                productoId = ProductoId(item.idProducto),
                nombreProducto = nombresProductos[item.idProducto] ?: "Producto #${item.idProducto}",
                detalle = DetalleCompra(
                    cantidad = Cantidad(if (item.cantidadProducto <= 0) 1 else item.cantidadProducto),
                    costoUnitario = Dinero(item.parcialCompra)
                )
            )
        }

        return Compra(
            id = CompraId(entity.idCompra),
            informacion = InformacionCompra(
                fechaCompra = entity.fechaCompra,
                estado = EstadoCompra.REGISTRADA,
                total = Dinero(entity.totalCompra)
            ),
            productos = productosComprados
        )
    }

    override fun toPersistence(domain: Compra): CompraEntity {
        return CompraEntity(
            idCompra = domain.id.valor,
            totalCompra = domain.informacion.total.monto,
            fechaCompra = domain.informacion.fechaCompra
        )
    }

    fun toItemPersistence(item: ProductoComprado, compraId: CompraId): CompraProductoEntity {
        return CompraProductoEntity(
            idCompraProducto = item.id.valor,
            parcialCompra = item.detalle.costoUnitario.monto,
            cantidadProducto = item.detalle.cantidad.valor,
            idCompra = compraId.valor,
            idProducto = item.productoId.valor
        )
    }
}
