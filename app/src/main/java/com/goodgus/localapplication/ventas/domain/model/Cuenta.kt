package com.goodgus.localapplication.ventas.domain.model

import com.goodgus.localapplication.core.domain.AggregateRoot
import com.goodgus.localapplication.core.domain.Cantidad
import com.goodgus.localapplication.core.domain.CuentaCerradaException
import com.goodgus.localapplication.core.domain.Dinero
import com.goodgus.localapplication.core.domain.VentaNoEncontradaException
import com.goodgus.localapplication.inventario.domain.model.Producto

/**
 * Raíz de Agregado (Aggregate Root) del Bounded Context de Ventas.
 * Gestiona el ciclo de vida de la cuenta/caja, sus ventas e integridad de balances.
 */
data class Cuenta(
    override val id: CuentaId,
    val informacion: InformacionCuenta,
    val ventas: List<Venta> = emptyList()
) : AggregateRoot<CuentaId> {

    val estaAbierta: Boolean get() = informacion.estaAbierta

    val totalCalculado: Dinero
        get() = ventas
            .filter { it.estado == EstadoVenta.ACTIVA }
            .map { it.subtotal }
            .fold(Dinero.CERO) { acc, subtotal -> acc + subtotal }

    /**
     * Regla de negocio: Registrar una venta en la cuenta activa y descontar stock.
     */
    fun agregarVenta(
        ventaId: VentaId,
        producto: Producto,
        cantidad: Cantidad,
        hora: String,
        precioUnitario: Dinero
    ): Result<Pair<Cuenta, Producto>> {
        if (!estaAbierta) {
            return Result.failure(CuentaCerradaException(id))
        }

        val descuentoResult = producto.descontarStock(cantidad.valor)
        if (descuentoResult.isFailure) {
            return Result.failure(descuentoResult.exceptionOrNull()!!)
        }

        val nuevaVenta = Venta(
            id = ventaId,
            productoId = producto.id,
            nombreProducto = producto.nombre,
            detalle = DetalleVenta(
                cantidad = cantidad,
                hora = hora,
                precioUnitario = precioUnitario
            ),
            estado = EstadoVenta.ACTIVA
        )

        val nuevasVentas = ventas + nuevaVenta
        val nuevoTotal = recalcularTotal(nuevasVentas)

        val cuentaActualizada = copy(
            informacion = informacion.copy(total = nuevoTotal),
            ventas = nuevasVentas
        )

        return Result.success(Pair(cuentaActualizada, descuentoResult.getOrThrow()))
    }

    /**
     * Regla de negocio: Cancelar/anular una línea de venta existente.
     */
    fun cancelarVenta(ventaId: VentaId): Result<Cuenta> {
        if (!estaAbierta) {
            return Result.failure(CuentaCerradaException(id))
        }

        val ventaIndex = ventas.indexOfFirst { it.id == ventaId }
        if (ventaIndex == -1) {
            return Result.failure(VentaNoEncontradaException(ventaId, id))
        }

        val ventasActualizadas = ventas.toMutableList()
        ventasActualizadas[ventaIndex] = ventasActualizadas[ventaIndex].cancelar()

        val nuevoTotal = recalcularTotal(ventasActualizadas)

        val cuentaActualizada = copy(
            informacion = informacion.copy(total = nuevoTotal),
            ventas = ventasActualizadas
        )

        return Result.success(cuentaActualizada)
    }

    /**
     * Regla de negocio: Cierre definitivo de cuenta / corte de caja.
     */
    fun cerrar(): Result<Cuenta> {
        if (!estaAbierta) {
            return Result.failure(CuentaCerradaException(id))
        }

        val totalFinal = recalcularTotal(ventas)
        val cuentaCerrada = copy(
            informacion = informacion.copy(
                estado = EstadoCuenta.CERRADA,
                total = totalFinal
            )
        )

        return Result.success(cuentaCerrada)
    }

    private fun recalcularTotal(listaVentas: List<Venta>): Dinero {
        return listaVentas
            .filter { it.estado == EstadoVenta.ACTIVA }
            .map { it.subtotal }
            .fold(Dinero.CERO) { acc, subtotal -> acc + subtotal }
    }
}
