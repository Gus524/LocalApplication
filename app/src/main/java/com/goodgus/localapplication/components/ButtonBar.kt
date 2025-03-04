package com.goodgus.localapplication.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.goodgus.localapplication.core.navigation.NavigationItem

/**
 * Composable de la barra de navegacion
 *
 * @param items recibe la lista de elementos de tipo NavigationItem que se mostraran en la navegacion
 * @param current ruta actual de la aplicacion
 * @param navController controlador de la navegacion de la aplicacion
 */

@Composable
fun AppButtonBar(
    items: List<NavigationItem>,
    current: String?,
    navController: NavController
) {
    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(item.icon, contentDescription = item.label)
                },
                selected = current == item.route.ruta,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(id = navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                    }
                },
                label = {
                    Text(item.label)
                }
            )
        }
    }
}