package com.goodgus.localapplication.inventario.domain

import com.goodgus.localapplication.core.domain.Dinero
import com.goodgus.localapplication.core.domain.ProductoInactivoException
import com.goodgus.localapplication.core.domain.StockInsuficienteException
import com.goodgus.localapplication.inventario.domain.model.EstadoProducto
import com.goodgus.localapplication.inventario.domain.model.InformacionProducto
import com.goodgus.localapplication.inventario.domain.model.Inventario
import com.goodgus.localapplication.inventario.domain.model.Producto
import com.goodgus.localapplication.inventario.domain.model.ProductoId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductoTest {

    private fun crearProducto(
        id: Int = 1,
        nombre: String = "Galletas",
        marca: String = "Gamesa",
        tipo: String = "Abarrotes",
        precio: Double = 18.50,
        stock: Int = 10,
        estado: EstadoProducto = EstadoProducto.ACTIVO
    ): Producto {
        return Producto(
            id = ProductoId(id),
            informacion = InformacionProducto(nombre, marca, tipo),
            precioVenta = Dinero(precio),
            inventario = Inventario(stock),
            estado = estado
        )
    }

    @Test
    fun `no permite crear ProductoId negativo`() {
        assertThrows(IllegalArgumentException::class.java) {
            ProductoId(-1)
        }
    }

    @Test
    fun `no permite crear informacion de producto con nombre vacio`() {
        assertThrows(IllegalArgumentException::class.java) {
            InformacionProducto(nombre = "  ", marca = "Marca", tipo = "Tipo")
        }
    }

    @Test
    fun `descontar stock actualiza el inventario del producto exitosamente`() {
        val producto = crearProducto(stock = 10)
        val resultado = producto.descontarStock(3)

        assertTrue(resultado.isSuccess)
        val productoActualizado = resultado.getOrThrow()
        assertEquals(7, productoActualizado.inventario.disponibles)
        assertEquals(10, producto.inventario.disponibles) // Inmutabilidad
    }

    @Test
    fun `descontar stock insuficiente retorna fallo con StockInsuficienteException`() {
        val producto = crearProducto(stock = 2)
        val resultado = producto.descontarStock(5)

        assertTrue(resultado.isFailure)
        assertTrue(resultado.exceptionOrNull() is StockInsuficienteException)
    }

    @Test
    fun `descontar stock a producto inactivo retorna fallo con ProductoInactivoException`() {
        val producto = crearProducto(stock = 10, estado = EstadoProducto.INACTIVO)
        val resultado = producto.descontarStock(1)

        assertTrue(resultado.isFailure)
        assertTrue(resultado.exceptionOrNull() is ProductoInactivoException)
    }

    @Test
    fun `reabastecer producto incrementa existencias`() {
        val producto = crearProducto(stock = 5)
        val productoActualizado = producto.reabastecer(10)

        assertEquals(15, productoActualizado.inventario.disponibles)
    }

    @Test
    fun `actualizar precio modifica el precio de venta`() {
        val producto = crearProducto(precio = 20.0)
        val productoActualizado = producto.actualizarPrecio(Dinero(25.0))

        assertEquals(25.0, productoActualizado.precioVenta.monto, 0.001)
    }

    @Test
    fun `activar y desactivar producto modifican su estado`() {
        val producto = crearProducto(estado = EstadoProducto.ACTIVO)
        val desactivado = producto.desactivar()
        val reactivado = desactivado.activar()

        assertEquals(EstadoProducto.INACTIVO, desactivado.estado)
        assertEquals(EstadoProducto.ACTIVO, reactivado.estado)
    }
}
