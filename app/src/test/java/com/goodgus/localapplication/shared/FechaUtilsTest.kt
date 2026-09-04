package com.goodgus.localapplication.shared

import com.goodgus.localapplication.shared.utilidades.FechaUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FechaUtilsTest {

    @Test
    fun `formatearAUi convierte formato ISO yyyy-MM-dd a dd-MM-yyyy correctamente`() {
        val resultado = FechaUtils.formatearAUi("2026-09-03")
        assertEquals("03-09-2026", resultado)
    }

    @Test
    fun `formatearAUi preserva formato si ya es dd-MM-yyyy`() {
        val resultado = FechaUtils.formatearAUi("15-12-2026")
        assertEquals("15-12-2026", resultado)
    }

    @Test
    fun `formatearAUi maneja valores nulos o vacios`() {
        assertEquals("", FechaUtils.formatearAUi(null))
        assertEquals("", FechaUtils.formatearAUi(""))
        assertEquals("", FechaUtils.formatearAUi("   "))
    }

    @Test
    fun `formatearAIso convierte formato dd-MM-yyyy a yyyy-MM-dd correctamente`() {
        val resultado = FechaUtils.formatearAIso("03-09-2026")
        assertEquals("2026-09-03", resultado)
    }

    @Test
    fun `formatearAIso preserva formato si ya es ISO`() {
        val resultado = FechaUtils.formatearAIso("2026-11-20")
        assertEquals("2026-11-20", resultado)
    }

    @Test
    fun `formatearAIso retorna null para valores nulos o vacios`() {
        assertNull(FechaUtils.formatearAIso(null))
        assertNull(FechaUtils.formatearAIso(""))
        assertNull(FechaUtils.formatearAIso("   "))
    }
}
