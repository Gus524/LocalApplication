package com.goodgus.localapplication.shared.utilidades

import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Utilidades para formateo y conversión segura de fechas entre el formato ISO (dominio/base de datos)
 * y el formato legible de usuario (dd-MM-yyyy).
 */
object FechaUtils {
    val FORMATO_UI: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    val FORMATO_ISO: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun formatearAUi(fechaIso: String?): String {
        if (fechaIso.isNullOrBlank()) return ""
        return try {
            val date = LocalDate.parse(fechaIso.trim(), FORMATO_ISO)
            date.format(FORMATO_UI)
        } catch (_: Exception) {
            try {
                LocalDate.parse(fechaIso.trim(), FORMATO_UI).format(FORMATO_UI)
            } catch (_: Exception) {
                fechaIso
            }
        }
    }

    fun formatearAIso(fechaUi: String?): String? {
        if (fechaUi.isNullOrBlank()) return null
        return try {
            val date = LocalDate.parse(fechaUi.trim(), FORMATO_UI)
            date.format(FORMATO_ISO)
        } catch (_: Exception) {
            try {
                LocalDate.parse(fechaUi.trim(), FORMATO_ISO).format(FORMATO_ISO)
            } catch (_: Exception) {
                fechaUi
            }
        }
    }

    fun hoyUi(): String = LocalDate.now().format(FORMATO_UI)
    fun hoyIso(): String = LocalDate.now().format(FORMATO_ISO)
}
