package com.goodgus.localapplication.core.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationLogicTest {

    @Test
    fun `verificar que todas las rutas implementan NavKey`() {
        val routes: List<AppRoute> = listOf(
            AppRoute.Home,
            AppRoute.Inventario,
            AppRoute.Pedidos,
            AppRoute.Compras,
            AppRoute.Venta(idVenta = "v-1"),
            AppRoute.Producto(idProducto = "p-1"),
            AppRoute.EditPedido(idPedido = "ped-1"),
            AppRoute.CompraProducto(idCompra = "c-1")
        )

        routes.forEach { route ->
            assertTrue("${route::class.simpleName} debe implementar NavKey", route is NavKey)
        }
    }

    @Test
    fun `verificar titulos correspondientes para cada ruta y parametros`() {
        assertEquals("Cuenta", AppRoute.Home.title)
        assertEquals("Inventario", AppRoute.Inventario.title)
        assertEquals("Pedidos", AppRoute.Pedidos.title)
        assertEquals("Compras", AppRoute.Compras.title)

        // Ventas
        assertEquals("Nueva Venta", AppRoute.Venta().title)
        assertEquals("Actualizar Venta", AppRoute.Venta(idVenta = "123").title)

        // Productos
        assertEquals("Nuevo Producto", AppRoute.Producto().title)
        assertEquals("Editar Producto", AppRoute.Producto(idProducto = "prod-abc").title)

        // Pedidos
        assertEquals("Nuevo Pedido", AppRoute.EditPedido().title)
        assertEquals("Editar Pedido", AppRoute.EditPedido(idPedido = "ped-xyz").title)

        // Compras
        assertEquals("Detalle Compra", AppRoute.CompraProducto(idCompra = "compra-1").title)
    }

    @Test
    fun `verificar flujo de navegacion push y pop en backStack de Navigation 3`() {
        val backStack = mutableListOf<NavKey>(AppRoute.Home)

        assertEquals(AppRoute.Home, backStack.lastOrNull())
        assertEquals(1, backStack.size)

        // Push a Venta
        val ventaRoute = AppRoute.Venta(idVenta = "venta-100")
        backStack.add(ventaRoute)

        assertEquals(2, backStack.size)
        assertEquals(ventaRoute, backStack.lastOrNull())
        assertEquals("Actualizar Venta", (backStack.lastOrNull() as? AppRoute)?.title)

        // Pop vuelve a Home
        val popped = backStack.removeLastOrNull()
        assertEquals(ventaRoute, popped)
        assertEquals(1, backStack.size)
        assertEquals(AppRoute.Home, backStack.lastOrNull())
    }

    @Test
    fun `verificar comportamiento de root-swap para cambio de tabs`() {
        val backStack = mutableListOf<NavKey>(AppRoute.Home)

        // Navegamos profundo dentro de una pestaña
        backStack.add(AppRoute.Venta(idVenta = "v-1"))
        backStack.add(AppRoute.Producto(idProducto = "p-1"))
        assertEquals(3, backStack.size)

        // Simular switch de tab a Inventario (root-swap)
        val targetTab: AppRoute = AppRoute.Inventario
        while (backStack.size > 1) {
            backStack.removeLastOrNull()
        }
        backStack[0] = targetTab

        assertEquals(1, backStack.size)
        assertEquals(AppRoute.Inventario, backStack.lastOrNull())
        assertEquals("Inventario", (backStack.lastOrNull() as? AppRoute)?.title)
    }

    @Test
    fun `verificar serializacion y deserializacion de rutas para restauracion de estado`() {
        val json = Json { prettyPrint = true }

        // Test Venta
        val venta = AppRoute.Venta(idVenta = "v-999")
        val ventaJson = json.encodeToString(venta)
        val decodedVenta = json.decodeFromString<AppRoute.Venta>(ventaJson)
        assertEquals("v-999", decodedVenta.idVenta)

        // Test Producto
        val producto = AppRoute.Producto(idProducto = "p-555")
        val productoJson = json.encodeToString(producto)
        val decodedProducto = json.decodeFromString<AppRoute.Producto>(productoJson)
        assertEquals("p-555", decodedProducto.idProducto)

        // Test EditPedido
        val pedido = AppRoute.EditPedido(idPedido = "ped-333")
        val pedidoJson = json.encodeToString(pedido)
        val decodedPedido = json.decodeFromString<AppRoute.EditPedido>(pedidoJson)
        assertEquals("ped-333", decodedPedido.idPedido)

        // Test CompraProducto
        val compra = AppRoute.CompraProducto(idCompra = "c-777")
        val compraJson = json.encodeToString(compra)
        val decodedCompra = json.decodeFromString<AppRoute.CompraProducto>(compraJson)
        assertEquals("c-777", decodedCompra.idCompra)
    }
}
