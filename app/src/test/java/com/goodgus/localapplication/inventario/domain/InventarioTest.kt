package com.goodgus.localapplication.inventario.domain

import com.goodgus.localapplication.inventario.domain.model.Inventario
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class InventarioTest {

    @Test
    fun `no debe permitir existencias negativas`() {
        assertThrows(IllegalArgumentException::class.java) {
            Inventario(-1)
        }
    }

    @Test
    fun `verificar disponibilidad correctamente`() {
        val stock = Inventario(10)

        assertTrue(stock.tieneExistenciasPara(5))
        assertTrue(stock.tieneExistenciasPara(10))
        assertFalse(stock.tieneExistenciasPara(11))
    }

    @Test
    fun `descontar existencias retorna un nuevo inventario con el remanente`() {
        val stockInicial = Inventario(10)
        val stockFinal = stockInicial.descontar(4)

        assertEquals(6, stockFinal.disponibles)
        assertEquals(10, stockInicial.disponibles) // Inmutabilidad
    }

    @Test
    fun `descontar mas de lo disponible lanza excepcion`() {
        val stock = Inventario(5)
        assertThrows(IllegalArgumentException::class.java) {
            stock.descontar(6)
        }
    }

    @Test
    fun `reabastecer existencias suma correctamente las unidades`() {
        val stockInicial = Inventario(5)
        val stockFinal = stockInicial.reabastecer(10)

        assertEquals(15, stockFinal.disponibles)
    }

    @Test
    fun `reabastecer con cero o negativo lanza excepcion`() {
        val stock = Inventario(5)
        assertThrows(IllegalArgumentException::class.java) {
            stock.reabastecer(0)
        }
        assertThrows(IllegalArgumentException::class.java) {
            stock.reabastecer(-2)
        }
    }
}
