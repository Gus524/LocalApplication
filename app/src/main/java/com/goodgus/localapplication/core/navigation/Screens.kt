package com.goodgus.localapplication.core.navigation

import kotlinx.serialization.Serializable

/**
 * Objetos los cuales forman parte de la navegacion de nuestra aplicacion
 * Son las pantallas con sus rutas y titulos
 *
 * Algunas llevan parametros para mostrar pantallas con datos especificos
 * TODO revisar para implementarlo como data object o data class, es decir que todas sean del mismo tipo
 */

@Serializable
data object Home : Ruta("Home", "Cuenta")

@Serializable
data object Inventario : Ruta("Inventario", "Inventario")

@Serializable
data object Compras : Ruta("Compras", "Compras")

@Serializable
data object Pedidos : Ruta("Pedidos", "Pedidos")

@Serializable
data class Venta(val idVenta: String?) : Ruta("Venta", "Venta")

@Serializable
data class Producto(val idProducto: String?)

@Serializable
data class EditPedido(val idPedido: String?)

@Serializable
data class CompraProducto(val idCompra: String)

// Agregamos una clase sellada para facilitar la implementacion de destinos y propiedades
@Serializable
sealed class Ruta(val ruta: String, val title: String)