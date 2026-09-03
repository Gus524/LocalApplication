package com.goodgus.localapplication.inventario.data.mapper

import com.goodgus.localapplication.common.data.mapper.IMapper
import com.goodgus.localapplication.common.domain.Dinero
import com.goodgus.localapplication.inventario.domain.model.EstadoProducto
import com.goodgus.localapplication.inventario.domain.model.InformacionProducto
import com.goodgus.localapplication.inventario.domain.model.Inventario
import com.goodgus.localapplication.inventario.domain.model.Producto
import com.goodgus.localapplication.inventario.domain.model.ProductoId
import com.goodgus.localapplication.models.data.Producto as ProductoEntity

class ProductoMapper : IMapper<Producto, ProductoEntity> {
    override fun toDomain(entity: ProductoEntity): Producto {
        return Producto(
            id = ProductoId(entity.idProducto),
            informacion = InformacionProducto(
                nombre = entity.nombre,
                marca = entity.marca,
                tipo = entity.tipo
            ),
            precioVenta = Dinero(entity.precioVenta),
            inventario = Inventario(entity.disponibles),
            estado = if (entity.estado == 1) EstadoProducto.ACTIVO else EstadoProducto.INACTIVO
        )
    }

    override fun toPersistence(domain: Producto): ProductoEntity {
        return ProductoEntity(
            idProducto = domain.id.valor,
            nombre = domain.nombre,
            marca = domain.marca,
            precioVenta = domain.precioVenta.monto,
            disponibles = domain.inventario.disponibles,
            tipo = domain.tipo,
            estado = if (domain.estaActivo) 1 else 0
        )
    }
}
