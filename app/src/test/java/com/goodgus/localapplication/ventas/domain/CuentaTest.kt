package com.goodgus.localapplication.ventas.domain

import com.goodgus.localapplication.core.domain.Cantidad
import com.goodgus.localapplication.core.domain.CuentaCerradaException
import com.goodgus.localapplication.core.domain.Dinero
import com.goodgus.localapplication.core.domain.StockInsuficienteException
import com.goodgus.localapplication.core.domain.VentaNoEncontradaException
import com.goodgus.localapplication.inventario.domain.model.EstadoProducto
import com.goodgus.localapplication.inventario.domain.model.InformacionProducto
import com.goodgus.localapplication.inventario.domain.model.Inventario
import com.goodgus.localapplication.inventario.domain.model.Producto
import com.goodgus.localapplication.inventario.domain.model.ProductoId
import com.goodgus.localapplication.ventas.domain.model.Cuenta
import com.goodgus.localapplication.ventas.domain.model.CuentaId
import com.goodgus.localapplication.ventas.domain.model.EstadoCuenta
import com.goodgus.localapplication.ventas.domain.model.EstadoVenta
import com.goodgus.localapplication.ventas.domain.model.InformacionCuenta
import com.goodgus.localapplication.ventas.domain.model.VentaId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CuentaTest {

    private fun crearProducto(
        id: Int = 1,
        nombre: String = "Refresco",
        precio: Double = 20.0,
        stock: Int = 10
    ): Producto {
        return Producto(
            id = ProductoId(id),
            informacion = InformacionProducto(nombre, "Coca-Cola", "Bebidas"),
            precioVenta = Dinero(precio),
            inventario = Inventario(stock),
            estado = EstadoProducto.ACTIVO
        )
    }

    private fun crearCuenta(
        id: Int = 1,
        fecha: String = "2026-09-02",
        estado: EstadoCuenta = EstadoCuenta.ABIERTA
    ): Cuenta {
        return Cuenta(
            id = CuentaId(id),
            informacion = InformacionCuenta(fecha = fecha, estado = estado)
        )
    }

    @Test
    fun `agregar venta con precio de producto actualiza stock y total de la cuenta`() {
        val cuenta = crearCuenta()
        val producto = crearProducto(precio = 20.0, stock = 10)

        val resultado = cuenta.agregarVenta(
            ventaId = VentaId(101),
            producto = producto,
            cantidad = Cantidad(2),
            hora = "10:15",
            precioUnitario = producto.precioVenta
        )

        assertTrue(resultado.isSuccess)
        val (cuentaActualizada, productoActualizado) = resultado.getOrThrow()

        assertEquals(8, productoActualizado.inventario.disponibles)
        assertEquals(1, cuentaActualizada.ventas.size)
        assertEquals(40.0, cuentaActualizada.informacion.total.monto, 0.001)
        assertEquals(40.0, cuentaActualizada.totalCalculado.monto, 0.001)
    }

    @Test
    fun `agregar venta con precio personalizado respeta el descuento y calcula total`() {
        val cuenta = crearCuenta()
        val producto = crearProducto(precio = 20.0, stock = 10)

        // Precio con descuento especial a 15.0
        val resultado = cuenta.agregarVenta(
            ventaId = VentaId(102),
            producto = producto,
            cantidad = Cantidad(3),
            hora = "11:00",
            precioUnitario = Dinero(15.0)
        )

        assertTrue(resultado.isSuccess)
        val (cuentaActualizada, _) = resultado.getOrThrow()

        assertEquals(45.0, cuentaActualizada.informacion.total.monto, 0.001)
    }

    @Test
    fun `agregar venta a cuenta cerrada falla con CuentaCerradaException`() {
        val cuenta = crearCuenta(estado = EstadoCuenta.CERRADA)
        val producto = crearProducto(stock = 10)

        val resultado = cuenta.agregarVenta(
            ventaId = VentaId(103),
            producto = producto,
            cantidad = Cantidad(1),
            hora = "12:00",
            precioUnitario = producto.precioVenta
        )

        assertTrue(resultado.isFailure)
        assertTrue(resultado.exceptionOrNull() is CuentaCerradaException)
    }

    @Test
    fun `agregar venta con stock insuficiente falla con StockInsuficienteException`() {
        val cuenta = crearCuenta()
        val producto = crearProducto(stock = 1)

        val resultado = cuenta.agregarVenta(
            ventaId = VentaId(104),
            producto = producto,
            cantidad = Cantidad(5),
            hora = "12:30",
            precioUnitario = producto.precioVenta
        )

        assertTrue(resultado.isFailure)
        assertTrue(resultado.exceptionOrNull() is StockInsuficienteException)
    }

    @Test
    fun `cancelar venta actualiza estado y descuenta del total`() {
        val cuenta = crearCuenta()
        val producto = crearProducto(precio = 10.0, stock = 10)

        val (cuentaConVenta, _) = cuenta.agregarVenta(
            ventaId = VentaId(201),
            producto = producto,
            cantidad = Cantidad(3),
            hora = "13:00",
            precioUnitario = producto.precioVenta
        ).getOrThrow()

        assertEquals(30.0, cuentaConVenta.informacion.total.monto, 0.001)

        val resultadoCancelacion = cuentaConVenta.cancelarVenta(VentaId(201))
        assertTrue(resultadoCancelacion.isSuccess)
        val cuentaCancelada = resultadoCancelacion.getOrThrow()

        assertEquals(0.0, cuentaCancelada.informacion.total.monto, 0.001)
        assertEquals(EstadoVenta.CANCELADA, cuentaCancelada.ventas.first().estado)
    }

    @Test
    fun `cancelar venta inexistente retorna fallo con VentaNoEncontradaException`() {
        val cuenta = crearCuenta()
        val resultado = cuenta.cancelarVenta(VentaId(999))

        assertTrue(resultado.isFailure)
        assertTrue(resultado.exceptionOrNull() is VentaNoEncontradaException)
    }

    @Test
    fun `cerrar cuenta transiciona su estado a CERRADA`() {
        val cuenta = crearCuenta()
        val resultado = cuenta.cerrar()

        assertTrue(resultado.isSuccess)
        val cuentaCerrada = resultado.getOrThrow()
        assertEquals(EstadoCuenta.CERRADA, cuentaCerrada.informacion.estado)
        assertFalse(cuentaCerrada.estaAbierta)
    }

    @Test
    fun `cerrar cuenta ya cerrada retorna fallo con CuentaCerradaException`() {
        val cuenta = crearCuenta(estado = EstadoCuenta.CERRADA)
        val resultado = cuenta.cerrar()

        assertTrue(resultado.isFailure)
        assertTrue(resultado.exceptionOrNull() is CuentaCerradaException)
    }
}
