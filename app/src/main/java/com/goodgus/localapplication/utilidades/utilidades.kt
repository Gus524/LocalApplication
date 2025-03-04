package com.goodgus.localapplication.utilidades

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.goodgus.localapplication.application.LocalApplication
import com.goodgus.localapplication.core.navigation.Compras
import com.goodgus.localapplication.core.navigation.Home
import com.goodgus.localapplication.core.navigation.Inventario
import com.goodgus.localapplication.core.navigation.Pedidos
import com.goodgus.localapplication.core.navigation.Ruta
import com.goodgus.localapplication.core.navigation.Venta

/**
    Clase para funciones genericas
 */

/**
 * Funcion para obtener le titulo dependiendo la pagina
 *
 * @param current la ruta actual de las paginas
 *
 * @return retorna el titulo de la pagina
 */

fun getTitle(current: String): String {
    return when (current) {
        "Home" -> Home.title
        "Inventario" -> Inventario.title
        "Compras" -> Compras.title
        "Pedidos" -> Pedidos.title
        else -> "Producto"
    }
}

/**
 * Funcion para extraer la ruta ya que se utilizan como objetos
 *
 * @param la ruta completa de la pantalla
 *
 * @return retorna el ultimo string de la cadena completa
 *
 */

fun extractRuta(ruta: String?): String? {
    return ruta?.substringAfterLast(".")
}

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
