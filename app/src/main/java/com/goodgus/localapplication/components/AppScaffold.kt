package com.goodgus.localapplication.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.goodgus.localapplication.core.navigation.AppNavigation
import com.goodgus.localapplication.core.navigation.navigationBarScreens

/**
 * @Composable del scaffold de la aplicacion
 *
 * @param navController recibe el controlador de la navegacion
 * @param currentRoute Ruta actual de la aplicacion
 * @param searchText en desuso actualmente, se utilizaba para el buscador
 * @param onTextChange Evento cuando cambia el texto buscado
 * @param showSearch
 * @param title Titulo de la pantalla actual
 * @param content Contenido central del Scaffold
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    navController: NavController,
    currentRoute: String,
    searchText: String,
    onTextChange: (String) -> Unit,
    showSearch: Boolean = false,
    title: String,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold (
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
            if(showNavigationBar) {
                AppNavigation(
                    navController = navController,
                    currentRoute = currentRoute
                )
            }
        }, content = content
    )
}