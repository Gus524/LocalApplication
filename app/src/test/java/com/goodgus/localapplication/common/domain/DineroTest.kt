package com.goodgus.localapplication.common.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class DineroTest {

    @Test
    fun `no debe permitir montos negativos`() {
        assertThrows(IllegalArgumentException::class.java) {
            Dinero(-10.0)
        }
    }

    @Test
    fun `permite monto cero y positivo`() {
        val cero = Dinero.CERO
        val diez = Dinero(10.50)

        assertEquals(0.0, cero.monto, 0.001)
        assertEquals(10.50, diez.monto, 0.001)
    }

    @Test
    fun `suma de dineros devuelve un nuevo dinero con la suma exacta`() {
        val d1 = Dinero(15.50)
        val d2 = Dinero(4.50)
        val resultado = d1 + d2

        assertEquals(20.0, resultado.monto, 0.001)
    }

    @Test
    fun `multiplicacion por cantidad entera devuelve el total correcto`() {
        val precio = Dinero(12.50)
        val total = precio * 3

        assertEquals(37.50, total.monto, 0.001)
    }
}
