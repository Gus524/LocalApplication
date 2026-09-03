package com.goodgus.localapplication.shared.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import com.goodgus.localapplication.core.navigation.AppNavigation
import com.goodgus.localapplication.core.navigation.AppRoute

/**
 * Lista de pantallas principales donde se muestra la barra de navegación inferior.
 */
val navigationBarScreens: List<AppRoute> = listOf(
    AppRoute.Home,
    AppRoute.Inventario,
    AppRoute.Pedidos,
    AppRoute.Compras
)

/**
 * @Composable del scaffold de la aplicacion
 *
 * @param currentRoute Ruta actual tipada [AppRoute] de la aplicacion
 * @param onTabSelected Callback invocado al seleccionar una pestaña
 * @param searchText en desuso actualmente, se utilizaba para el buscador
 * @param onTextChange Evento cuando cambia el texto buscado
 * @param showSearch si se muestra barra de búsqueda
 * @param title Titulo de la pantalla actual (por defecto [AppRoute.title])
 * @param content Contenido central del Scaffold
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    currentRoute: AppRoute,
    onTabSelected: (AppRoute) -> Unit,
    searchText: String = "",
    onTextChange: (String) -> Unit = {},
    showSearch: Boolean = false,
    title: String = currentRoute.title,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            if (showSearch) {
                ExpandableSearchBar(
                    searchText = searchText,
                    onTextChange = onTextChange,
                    title = title,
                )
            } else {
                TopAppBar(
                    title = { Text(text = title) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                )
            }
        },
        bottomBar = {
            val showNavigationBar = navigationBarScreens.contains(currentRoute)
            if (showNavigationBar) {
                AppNavigation(
                    currentRoute = currentRoute,
                    onTabSelected = onTabSelected
                )
            }
        },
        content = content
    )
}