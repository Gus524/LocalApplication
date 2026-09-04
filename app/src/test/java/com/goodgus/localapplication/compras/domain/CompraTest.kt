package com.goodgus.localapplication.compras.domain

import com.goodgus.localapplication.core.domain.Cantidad
import com.goodgus.localapplication.core.domain.CompraYaFinalizadaException
import com.goodgus.localapplication.core.domain.Dinero
import com.goodgus.localapplication.core.domain.ProductoInactivoException
import com.goodgus.localapplication.compras.domain.model.Compra
import com.goodgus.localapplication.compras.domain.model.CompraId
import com.goodgus.localapplication.compras.domain.model.DetalleCompra
import com.goodgus.localapplication.compras.domain.model.EstadoCompra
import com.goodgus.localapplication.compras.domain.model.InformacionCompra
import com.goodgus.localapplication.compras.domain.model.ProductoCompradoId
import com.goodgus.localapplication.inventario.domain.model.EstadoProducto
import com.goodgus.localapplication.inventario.domain.model.InformacionProducto
import com.goodgus.localapplication.inventario.domain.model.Inventario
import com.goodgus.localapplication.inventario.domain.model.Producto
import com.goodgus.localapplication.inventario.domain.model.ProductoId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class CompraTest {

    private fun crearProducto(
        id: Int = 1,
        nombre: String = "Harina de Trigo",
        precioVenta: Double = 30.0,
        stock: Int = 5,
        estado: EstadoProducto = EstadoProducto.ACTIVO
    ): Producto {
        return Producto(
            id = ProductoId(id),
            informacion = InformacionProducto(nombre, "Abarrotes", "Insumos"),
            precioVenta = Dinero(precioVenta),
            inventario = Inventario(stock),
            estado = estado
        )
    }

    private fun crearCompra(
        id: Int = 1,
        fecha: String = "2026-09-02",
        estado: EstadoCompra = EstadoCompra.REGISTRADA
    ): Compra {
        return Compra(
            id = CompraId(id),
            informacion = InformacionCompra(fechaCompra = fecha, estado = estado)
        )
    }

    @Test
    fun `CompraId y ProductoCompradoId rechazan valores negativos`() {
        assertThrows(IllegalArgumentException::class.java) {
            CompraId(-1)
        }
        assertThrows(IllegalArgumentException::class.java) {
            ProductoCompradoId(-5)
        }
    }

    @Test
    fun `DetalleCompra calcula subtotal inmutable correctamente`() {
        val detalle = DetalleCompra(
            cantidad = Cantidad(4),
            costoUnitario = Dinero(15.50)
        )
        assertEquals(62.0, detalle.subtotal.monto, 0.001)
    }

    @Test
    fun `InformacionCompra rechaza fecha vacia`() {
        assertThrows(IllegalArgumentException::class.java) {
            InformacionCompra(fechaCompra = "   ")
        }
    }

    @Test
    fun `agregar producto comprado reabastece stock y acumula total en compra`() {
        val compra = crearCompra()
        val producto = crearProducto(stock = 5)

        val resultado = compra.agregarProducto(
            lineaId = ProductoCompradoId(101),
            producto = producto,
            cantidad = Cantidad(10),
            costoUnitario = Dinero(12.0)
        )

        assertTrue(resultado.isSuccess)
        val (compraActualizada, productoActualizado) = resultado.getOrThrow()

        // Invariante de reabastecimiento: 5 iniciales + 10 comprados = 15 disponibles
        assertEquals(15, productoActualizado.inventario.disponibles)
        assertEquals(1, compraActualizada.productos.size)
        assertEquals(120.0, compraActualizada.informacion.total.monto, 0.001)
        assertEquals(120.0, compraActualizada.totalCalculado.monto, 0.001)
    }

    @Test
    fun `agregar multiples productos comprados acumula total correctamente`() {
        val compra = crearCompra()
        val producto1 = crearProducto(id = 1, stock = 2)
        val producto2 = crearProducto(id = 2, stock = 10)

        val (compraConP1, _) = compra.agregarProducto(
            lineaId = ProductoCompradoId(101),
            producto = producto1,
            cantidad = Cantidad(5),
            costoUnitario = Dinero(10.0) // 50.0
        ).getOrThrow()

        val (compraConAmbos, _) = compraConP1.agregarProducto(
            lineaId = ProductoCompradoId(102),
            producto = producto2,
            cantidad = Cantidad(2),
            costoUnitario = Dinero(25.0) // 50.0
        ).getOrThrow()

        assertEquals(2, compraConAmbos.productos.size)
        assertEquals(100.0, compraConAmbos.informacion.total.monto, 0.001)
        assertEquals(100.0, compraConAmbos.totalCalculado.monto, 0.001)
    }

    @Test
    fun `agregar producto comprado a producto inactivo falla con ProductoInactivoException`() {
        val compra = crearCompra()
        val productoInactivo = crearProducto(estado = EstadoProducto.INACTIVO)

        val resultado = compra.agregarProducto(
            lineaId = ProductoCompradoId(101),
            producto = productoInactivo,
            cantidad = Cantidad(5),
            costoUnitario = Dinero(10.0)
        )

        assertTrue(resultado.isFailure)
        assertTrue(resultado.exceptionOrNull() is ProductoInactivoException)
    }

    @Test
    fun `agregar producto a compra cancelada falla con CompraYaFinalizadaException`() {
        val compra = crearCompra(estado = EstadoCompra.CANCELADA)
        val producto = crearProducto()

        val resultado = compra.agregarProducto(
            lineaId = ProductoCompradoId(101),
            producto = producto,
            cantidad = Cantidad(5),
            costoUnitario = Dinero(10.0)
        )

        assertTrue(resultado.isFailure)
        assertTrue(resultado.exceptionOrNull() is CompraYaFinalizadaException)
    }

    @Test
    fun `cancelar compra transiciona estado a CANCELADA`() {
        val compra = crearCompra()
        val resultado = compra.cancelar()

        assertTrue(resultado.isSuccess)
        val compraCancelada = resultado.getOrThrow()
        assertEquals(EstadoCompra.CANCELADA, compraCancelada.informacion.estado)
        assertFalse(compraCancelada.estaRegistrada)
    }

    @Test
    fun `cancelar compra ya cancelada falla con CompraYaFinalizadaException`() {
        val compra = crearCompra(estado = EstadoCompra.CANCELADA)
        val resultado = compra.cancelar()

        assertTrue(resultado.isFailure)
        assertTrue(resultado.exceptionOrNull() is CompraYaFinalizadaException)
    }
}
