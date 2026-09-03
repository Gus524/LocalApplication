package com.goodgus.localapplication.core.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.goodgus.localapplication.common.components.AppScaffold
import com.goodgus.localapplication.compras.ui.views.CompraScreen
import com.goodgus.localapplication.inventario.ui.views.InventarioScreen
import com.goodgus.localapplication.inventario.ui.views.ProductoScreen
import com.goodgus.localapplication.pedidos.ui.views.EditPedidoScreen
import com.goodgus.localapplication.pedidos.ui.views.PedidoScreen
import com.goodgus.localapplication.utilidades.extractRuta
import com.goodgus.localapplication.utilidades.getTitle
import com.goodgus.localapplication.ventas.ui.views.VentaScreen
import com.goodgus.localapplication.views.HomeScreen

/**
 * Composable encargado de la navegacion de toda nuestra aplicacion
 */

@Composable
fun NavigationWrapper() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute: String = extractRuta(backStackEntry?.destination?.route) ?: Home.ruta

    AppScaffold(
        currentRoute = currentRoute,
        navController = navController,
        searchText = "",
        onTextChange = { },
        showSearch = false,
        title = getTitle(currentRoute),
        content = { padding ->
            NavHost(
                navController = navController,
                startDestination = Home,
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                composable<Home> {
                    HomeScreen(
                        navigateToVenta = { idVenta -> navController.navigate(Venta(idVenta = idVenta)) }
                    )
                }
                composable<Inventario> {
                    InventarioScreen(
                        navigateToProducto = { idProducto -> navController.navigate(Producto(idProducto = idProducto)) }
                    )
                }
                composable<Pedidos> {
                    PedidoScreen(
                        navigateToPedido = { navController.navigate(EditPedido("")) }
                    )
                }
                composable<Compras> {
                    CompraScreen(
                        navigateToDetalle = { idCompra -> navController.navigate(CompraProducto(idCompra = idCompra)) }
                    )
                }
                composable<Venta> { entry ->
                    val venta = entry.toRoute<Venta>()
                    VentaScreen(
                        idVenta = venta.idVenta,
                        navigateBack = {
                            navController.navigate(Home) {
                                popUpTo<Home>()
                            }
                        }
                    )
                }
                composable<Producto> { entry ->
                    val producto = entry.toRoute<Producto>()
                    ProductoScreen(
                        idProducto = producto.idProducto,
                        navigateBack = {
                            navController.navigate(Inventario) {
                                popUpTo<Inventario>()
                            }
                        }
                    )
                }
                composable<EditPedido> { entry ->
                    val pedido = entry.toRoute<EditPedido>()
                    EditPedidoScreen(
                        idPedido = pedido.idPedido,
                        navigateBack = {
                            navController.navigate(Pedidos) {
                                popUpTo<Pedidos>()
                            }
                        }
                    )
                }
            }
        }
    )
}

/**
 * Lista de pantallas en las cuales se mostrara el navigationBar
 */

val navigationBarScreens = listOf(
    Home.ruta,
    Inventario.ruta,
    Pedidos.ruta
//    Compras.ruta
)
