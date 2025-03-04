package com.goodgus.localapplication.core.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.goodgus.localapplication.components.AppScaffold
import com.goodgus.localapplication.utilidades.extractRuta
import com.goodgus.localapplication.utilidades.getContext
import com.goodgus.localapplication.utilidades.getTitle
import com.goodgus.localapplication.viewModels.EditPedidoViewModel
import com.goodgus.localapplication.viewModels.HomeViewModel
import com.goodgus.localapplication.viewModels.InventarioViewModel
import com.goodgus.localapplication.viewModels.PedidosViewModel
import com.goodgus.localapplication.viewModels.ProductoViewModel
import com.goodgus.localapplication.viewModels.VentaViewModel
import com.goodgus.localapplication.views.EditPedidoScreen
import com.goodgus.localapplication.views.HomeScreen
import com.goodgus.localapplication.views.InventarioScreen
import com.goodgus.localapplication.views.PedidoScreen
import com.goodgus.localapplication.views.ProductoScreen
import com.goodgus.localapplication.views.VentaScreen

/**
 * Composable encargado de la navegacion de toda nuestra aplicacion
 *
 * Se manejan las pantallas como objetos de tipo composable<>
 *
 *     TODO separar para que no este contenido aqui mismo el Scaffold, para separar el diseno del scaffold de la logica de navegacion
 *     ademas de implementar correctamente la creacion y evitar duplicacion de pantallas
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
                // Para cada pagina creamos un composable de tipo Screen.kt, agregamos la funcion de la clase <View> Screen
                composable<Home> {
                    // Obtenemos el contexto de la aplicacion para el viewModel
                    val viewModel = viewModel<HomeViewModel>(
                        factory = HomeViewModel.Factory(getContext())
                    )
                    // Codigo para navegacion con parametros
                    HomeScreen(
                        navigateToVenta = { idVenta -> navController.navigate(Venta(idVenta = idVenta))},
                        viewModel = viewModel
                    )
                }
                composable<Inventario>{
                    val viewModel = viewModel<InventarioViewModel>(
                        factory = InventarioViewModel.Factory(getContext())
                    )
                    InventarioScreen(
                        navigateToProducto = { idProducto -> navController.navigate(Producto(idProducto = idProducto))},
                        viewModel = viewModel
                    )
                }

                composable<Pedidos>{
                    val viewModel = viewModel<PedidosViewModel>(
                        factory = PedidosViewModel.Factory(getContext())
                    )
                    PedidoScreen(
                        navigateToPedido = { idPedido -> navController.navigate(EditPedido(""))},
                        viewModel = viewModel
                    )
                }
//                composable<Compras> {
//                    val viewModel = viewModel<CompraViewModel>(
//                        factory = CompraViewModel.Factory(getContext())
//                    )
//                    CompraScreen(
//                        navigateToDetalle = { idCompra -> navController.navigate(CompraProducto(idCompra = idCompra))},
//                        viewModel = viewModel
//                    )
//                }
                composable<Venta> { entry ->
                    val venta = entry.toRoute<Venta>()
                    val viewModel = viewModel<VentaViewModel>(
                        factory = VentaViewModel.Factory(getContext())
                    )

                    VentaScreen(
                        venta.idVenta,
                        navigateBack = { navController.navigate(Home){
                            popUpTo<Home>()
                        } },
                        viewModel = viewModel
                    )
                }

                composable<Producto> { entry ->
                    val producto = entry.toRoute<Producto>()
                    val viewModel = viewModel<ProductoViewModel>(
                        factory = ProductoViewModel.Factory(getContext())
                    )

                    ProductoScreen(
                        producto.idProducto,
                        navigateBack = { navController.navigate(Inventario){
                            popUpTo<Inventario>()
                        } },
                        viewModel = viewModel
                    )
                }

                composable<EditPedido> { entry ->
                    val pedido = entry.toRoute<EditPedido>()
                    val viewModel = viewModel<EditPedidoViewModel>(
                        factory = EditPedidoViewModel.Factory(getContext())
                    )

                    EditPedidoScreen(
                        pedido.idPedido,
                        navigateBack = { navController.navigate(Pedidos){
                            popUpTo<Pedidos>()
                        } },
                        viewModel = viewModel
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
