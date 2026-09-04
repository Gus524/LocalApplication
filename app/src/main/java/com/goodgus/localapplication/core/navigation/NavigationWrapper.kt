package com.goodgus.localapplication.core.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.goodgus.localapplication.shared.components.AppScaffold
import com.goodgus.localapplication.compras.ui.views.CompraScreen
import com.goodgus.localapplication.inventario.ui.views.InventarioScreen
import com.goodgus.localapplication.inventario.ui.views.ProductoScreen
import com.goodgus.localapplication.pedidos.ui.views.EditPedidoScreen
import com.goodgus.localapplication.pedidos.ui.views.PedidoScreen
import com.goodgus.localapplication.ventas.ui.views.VentaScreen
import com.goodgus.localapplication.ventas.ui.views.HomeScreen

/**
 * Composable encargado de la navegación de toda la aplicación utilizando Navigation 3.
 */
@Composable
fun NavigationWrapper() {
    val backStack = rememberNavBackStack(AppRoute.Home)
    val currentRoute: AppRoute = (backStack.lastOrNull() as? AppRoute) ?: AppRoute.Home


    val onNavigateToTab: (AppRoute) -> Unit = { targetRoute ->
        // Swap de raíz para pestañas principales
        while (backStack.size > 1) {
            backStack.removeLastOrNull()
        }
        backStack[0] = targetRoute
    }

    AppScaffold(
        currentRoute = currentRoute,
        onTabSelected = onNavigateToTab
    ) { padding ->
        NavDisplay(
            backStack = backStack,
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            entryProvider = entryProvider {
                entry<AppRoute.Home> {
                    HomeScreen(
                        navigateToVenta = { idVenta ->
                            backStack.add(AppRoute.Venta(idVenta = idVenta))
                        }
                    )
                }
                entry<AppRoute.Inventario> {
                    InventarioScreen(
                        navigateToProducto = { idProducto ->
                            backStack.add(AppRoute.Producto(idProducto = idProducto))
                        }
                    )
                }
                entry<AppRoute.Pedidos> {
                    PedidoScreen(
                        navigateToPedido = { idPedido ->
                            backStack.add(AppRoute.EditPedido(idPedido = idPedido.ifBlank { null }))
                        }
                    )
                }
//                entry<AppRoute.Compras> {
//                    CompraScreen(
//                        navigateToDetalle = { idCompra ->
//                            backStack.add(AppRoute.CompraProducto(idCompra = idCompra))
//                        }
//                    )
//                }
                entry<AppRoute.Venta> { ventaKey ->
                    VentaScreen(
                        idVenta = ventaKey.idVenta,
                        navigateBack = {
                            backStack.removeLastOrNull()
                        }
                    )
                }
                entry<AppRoute.Producto> { productoKey ->
                    ProductoScreen(
                        idProducto = productoKey.idProducto,
                        navigateBack = {
                            backStack.removeLastOrNull()
                        }
                    )
                }
                entry<AppRoute.EditPedido> { pedidoKey ->
                    EditPedidoScreen(
                        idPedido = pedidoKey.idPedido,
                        navigateBack = {
                            backStack.removeLastOrNull()
                        }
                    )
                }
            },
            transitionSpec = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(250)
                ) togetherWith slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(250)
                )
            },
            popTransitionSpec = {
                slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(250)
                ) togetherWith slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(250)
                )
            },
            predictivePopTransitionSpec = {
                slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(250)
                ) togetherWith slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(250)
                )
            }
        )
    }
}
