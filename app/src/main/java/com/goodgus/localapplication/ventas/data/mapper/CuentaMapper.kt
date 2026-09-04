package com.goodgus.localapplication.ventas.data.mapper

import com.goodgus.localapplication.core.data.mapper.IMapper
import com.goodgus.localapplication.core.domain.Cantidad
import com.goodgus.localapplication.core.domain.Dinero
import com.goodgus.localapplication.inventario.domain.model.ProductoId
import com.goodgus.localapplication.ventas.data.repository.Cuenta as CuentaEntity
import com.goodgus.localapplication.ventas.data.repository.Venta as VentaEntity
import com.goodgus.localapplication.ventas.domain.model.Cuenta
import com.goodgus.localapplication.ventas.domain.model.CuentaId
import com.goodgus.localapplication.ventas.domain.model.DetalleVenta
import com.goodgus.localapplication.ventas.domain.model.EstadoCuenta
import com.goodgus.localapplication.ventas.domain.model.EstadoVenta
import com.goodgus.localapplication.ventas.domain.model.InformacionCuenta
import com.goodgus.localapplication.ventas.domain.model.Venta
import com.goodgus.localapplication.ventas.domain.model.VentaId

class CuentaMapper : IMapper<Cuenta, CuentaEntity> {
    override fun toDomain(entity: CuentaEntity): Cuenta {
        return toDomain(entity, emptyList(), emptyMap())
    }

    fun toDomain(
        entity: CuentaEntity,
        ventasEntities: List<VentaEntity>,
        nombresProductos: Map<Int, String> = emptyMap()
    ): Cuenta {
        val ventasDominio = ventasEntities.map { v ->
            Venta(
                id = VentaId(v.idVenta),
                productoId = ProductoId(v.idProducto),
                nombreProducto = nombresProductos[v.idProducto] ?: "Producto #${v.idProducto}",
                detalle = DetalleVenta(
                    cantidad = Cantidad(if (v.cantidadProducto <= 0) 1 else v.cantidadProducto),
                    hora = v.horaVenta,
                    precioUnitario = Dinero(if (v.cantidadProducto > 0) v.parcialVenta / v.cantidadProducto else v.parcialVenta)
                ),
                estado = if (v.estadoVenta == 1) EstadoVenta.ACTIVA else EstadoVenta.CANCELADA
            )
        }

        return Cuenta(
            id = CuentaId(entity.idCuenta),
            informacion = InformacionCuenta(
                fecha = entity.fechaCuenta,
                estado = if (entity.estadoCuenta == 1) EstadoCuenta.ABIERTA else EstadoCuenta.CERRADA,
                total = Dinero(entity.totalCuenta)
            ),
            ventas = ventasDominio
        )
    }

    override fun toPersistence(domain: Cuenta): CuentaEntity {
        return CuentaEntity(
            idCuenta = domain.id.valor,
            fechaCuenta = domain.informacion.fecha,
            totalCuenta = domain.totalCalculado.monto,
            estadoCuenta = if (domain.estaAbierta) 1 else 0
        )
    }

    fun toVentaPersistence(venta: Venta, cuentaId: CuentaId): VentaEntity {
        return VentaEntity(
            idVenta = venta.id.valor,
            cantidadProducto = venta.detalle.cantidad.valor,
            horaVenta = venta.detalle.hora,
            parcialVenta = venta.subtotal.monto,
            estadoVenta = if (venta.estado == EstadoVenta.ACTIVA) 1 else 0,
            idProducto = venta.productoId.valor,
            idCuenta = cuentaId.valor
        )
    }
}
