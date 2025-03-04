package com.goodgus.localapplication.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import com.goodgus.localapplication.components.AppButtonBar

/**
 * Clase para generar los botones e iconos de la navegacion de pantallas principales
 *
 * Se debe asignar la ruta, el icono y el texto
 */

data class NavigationItem(
    val route: Ruta,
    val icon: ImageVector,
    val label: String
)

/**
 * Composable para mostrar los iconos de las pantallas principales
 *
 * @param navController El controlador de la navegacion
 * @param currentRoute La ruta actual (pantalla actual) de la aplicacion
 */

@Composable
fun AppNavigation(
    navController: NavController,
    currentRoute: String
    ) {
    val items = listOf(
        NavigationItem(Home, Icons.Filled.Home, "Cuenta"),
        NavigationItem(Inventario, Icons.AutoMirrored.Filled.List, "Inventario"),
        NavigationItem(Pedidos, Icons.Filled.Star, "Pedidos")
//        NavigationItem(Compras, Icons.Filled.ShoppingCart, "Compras")
    )

    AppButtonBar(
        items = items,
        current = currentRoute,
        navController = navController
    )
}

