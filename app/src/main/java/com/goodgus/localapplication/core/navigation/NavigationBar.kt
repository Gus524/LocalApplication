package com.goodgus.localapplication.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.goodgus.localapplication.shared.components.AppButtonBar

/**
 * Clase para generar los botones e iconos de la navegacion de pantallas principales
 *
 * Se debe asignar la ruta tipada [AppRoute], el icono y el texto
 */
data class NavigationItem(
    val route: AppRoute,
    val icon: ImageVector,
    val label: String
)

/**
 * Composable para mostrar los iconos de las pantallas principales
 *
 * @param currentRoute La ruta actual tipada [AppRoute] de la aplicacion
 * @param onTabSelected Callback invocado al seleccionar una pestaña
 */
@Composable
fun AppNavigation(
    currentRoute: AppRoute,
    onTabSelected: (AppRoute) -> Unit
) {
    val items = listOf(
        NavigationItem(AppRoute.Home, Icons.Filled.Home, "Cuenta"),
        NavigationItem(AppRoute.Inventario, Icons.AutoMirrored.Filled.List, "Inventario"),
        NavigationItem(AppRoute.Pedidos, Icons.Filled.Star, "Pedidos"),
        NavigationItem(AppRoute.Historial, Icons.AutoMirrored.Filled.ReceiptLong, "Historial")
    )

    AppButtonBar(
        items = items,
        current = currentRoute,
        onTabSelected = onTabSelected
    )
}


