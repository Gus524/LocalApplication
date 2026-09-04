package com.goodgus.localapplication.shared.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.goodgus.localapplication.core.navigation.AppRoute
import com.goodgus.localapplication.core.navigation.NavigationItem

/**
 * Composable de la barra de navegacion
 *
 * @param items recibe la lista de elementos de tipo [NavigationItem] que se mostraran en la navegacion
 * @param current ruta actual tipada [AppRoute] de la aplicacion
 * @param onTabSelected callback invocado al seleccionar una pestaña
 */
@Composable
fun AppButtonBar(
    items: List<NavigationItem>,
    current: AppRoute,
    onTabSelected: (AppRoute) -> Unit
) {
    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(item.icon, contentDescription = item.label)
                },
                selected = current == item.route,
                onClick = {
                    onTabSelected(item.route)
                },
                label = {
                    Text(item.label)
                }
            )
        }
    }
}