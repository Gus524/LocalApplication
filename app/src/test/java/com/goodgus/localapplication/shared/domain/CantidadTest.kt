package com.goodgus.localapplication.shared.domain

import com.goodgus.localapplication.core.domain.Cantidad
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class CantidadTest {

    @Test
    fun `no debe permitir cantidad cero o negativa`() {
        assertThrows(IllegalArgumentException::class.java) {
            Cantidad(0)
        }
        assertThrows(IllegalArgumentException::class.java) {
            Cantidad(-5)
        }
    }

    @Test
    fun `permite cantidad estrictamente positiva`() {
        val cantidad = Cantidad(5)
        assertEquals(5, cantidad.valor)
    }
}
