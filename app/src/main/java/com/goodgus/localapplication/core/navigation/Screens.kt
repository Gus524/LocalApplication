package com.goodgus.localapplication.core.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Contrato base para todas las rutas y pantallas de la aplicación.
 * Cada destino es serializable e implementa [AppRoute] garantizando tipado estático y robustez.
 */
sealed interface AppRoute : NavKey {
    val title: String

    @Serializable
    data object Home : AppRoute {
        override val title: String = "Cuenta"
    }

    @Serializable
    data object Inventario : AppRoute {
        override val title: String = "Inventario"
    }

    @Serializable
    data object Pedidos : AppRoute {
        override val title: String = "Pedidos"
    }

    @Serializable
    data object Compras : AppRoute {
        override val title: String = "Compras"
    }

    @Serializable
    data class Venta(val idVenta: String? = null) : AppRoute {
        override val title: String = if (idVenta != null) "Actualizar Venta" else "Nueva Venta"
    }

    @Serializable
    data class Producto(val idProducto: String? = null) : AppRoute {
        override val title: String = if (idProducto != null) "Editar Producto" else "Nuevo Producto"
    }

    @Serializable
    data class EditPedido(val idPedido: String? = null) : AppRoute {
        override val title: String = if (idPedido != null) "Editar Pedido" else "Nuevo Pedido"
    }

    @Serializable
    data class CompraProducto(val idCompra: String) : AppRoute {
        override val title: String = "Detalle Compra"
    }
}