package com.goodgus.localapplication.shared.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import com.goodgus.localapplication.core.navigation.AppNavigation
import com.goodgus.localapplication.core.navigation.AppRoute
import com.goodgus.localapplication.core.ui.components.feedback.SnackbarManager

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton

val LocalSnackbarHostState = compositionLocalOf<SnackbarHostState> {
    error("No SnackbarHostState provided")
}

/**
 * Lista de pantallas principales donde se muestra la barra de navegación inferior.
 */
val navigationBarScreens: List<AppRoute> = listOf(
    AppRoute.Home,
    AppRoute.Inventario,
    AppRoute.Pedidos,
    AppRoute.Historial
)

/**
 * @Composable del scaffold de la aplicacion
 *
 * @param currentRoute Ruta actual tipada [AppRoute] de la aplicacion
 * @param onTabSelected Callback invocado al seleccionar una pestaña
 * @param onNavigateBack Callback opcional invocado al presionar el botón de retroceso
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
    onNavigateBack: (() -> Unit)? = null,
    searchText: String = "",
    onTextChange: (String) -> Unit = {},
    showSearch: Boolean = false,
    title: String = currentRoute.title,
    content: @Composable (PaddingValues) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        SnackbarManager.mensajes.collect { mensaje ->
            snackbarHostState.showSnackbar(mensaje)
        }
    }

    val canNavigateBack = !navigationBarScreens.contains(currentRoute) && onNavigateBack != null

    CompositionLocalProvider(LocalSnackbarHostState provides snackbarHostState) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
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
                        navigationIcon = {
                            if (canNavigateBack) {
                                IconButton(onClick = onNavigateBack) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Regresar"
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
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
}