package com.goodgus.localapplication.ventas.domain

import com.goodgus.localapplication.common.domain.Cantidad
import com.goodgus.localapplication.common.domain.Dinero
import com.goodgus.localapplication.inventario.domain.model.ProductoId
import com.goodgus.localapplication.ventas.domain.model.DetalleVenta
import com.goodgus.localapplication.ventas.domain.model.EstadoVenta
import com.goodgus.localapplication.ventas.domain.model.Venta
import com.goodgus.localapplication.ventas.domain.model.VentaId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class VentaTest {

    @Test
    fun `calcula subtotal correctamente para venta activa`() {
        val detalle = DetalleVenta(
            cantidad = Cantidad(3),
            hora = "14:30",
            precioUnitario = Dinero(15.0)
        )
        val venta = Venta(
            id = VentaId(1),
            productoId = ProductoId(10),
            nombreProducto = "Sabritas",
            detalle = detalle,
            estado = EstadoVenta.ACTIVA
        )

        assertEquals(45.0, venta.subtotal.monto, 0.001)
    }

    @Test
    fun `subtotal de venta cancelada es cero`() {
        val detalle = DetalleVenta(
            cantidad = Cantidad(2),
            hora = "14:30",
            precioUnitario = Dinero(20.0)
        )
        val venta = Venta(
            id = VentaId(1),
            productoId = ProductoId(10),
            nombreProducto = "Sabritas",
            detalle = detalle,
            estado = EstadoVenta.ACTIVA
        )

        val ventaCancelada = venta.cancelar()

        assertEquals(EstadoVenta.CANCELADA, ventaCancelada.estado)
        assertEquals(0.0, ventaCancelada.subtotal.monto, 0.001)
    }
}
