package com.goodgus.localapplication.shared.utilidades

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.goodgus.localapplication.LocalApplication

/**
    Clase para funciones genericas
 */


/**
 * Composable que obtiene el contexto de la aplicacion
 *
 * @return retorna el contexto actual
 */

@Composable
fun getContext(): LocalApplication {
    return LocalContext.current.applicationContext as LocalApplication
}

// Función para formatear el precio con dos decimales
fun formatPrecio(precio: Double): String {
    return String.format("%.2f", precio)
}

// Función para convertir el texto ingresado a Double
fun parsePrecio(text: String): Double {
    return text.replace(",", ".").toDoubleOrNull() ?: 0.0
}
