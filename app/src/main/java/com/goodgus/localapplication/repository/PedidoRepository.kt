package com.goodgus.localapplication.repository

import com.goodgus.localapplication.DAO.PedidosDAO
import com.goodgus.localapplication.models.data.Pedidos
import kotlinx.coroutines.flow.Flow

/**
 * Clases de repositorio
 * Son la conexion entre los ViewModel y los DAO de nuestros modelos
 *
 * Implementan los modelos para las funciones y logica de negocio de nuestra aplicacion
 * Los repositorios pueden implementar mas de un DAO
 *
 */


/**
 * Clase PedidoRepository
 *
 * @param pedidosDAO es la interfaz DAO del modelo Pedidos
 *
 */

class PedidoRepository(private val pedidosDAO: PedidosDAO) {

    /**
     * Funcion addPedido implementa addPedido del Dao, agrega un nuevo pedido
     *
     * @param fechaPedido fecha en la que se realiza el pedido
     * @param descripcion La descripcion del pedido (titulo)
     * @param detalles detalles del pedido
     */

    fun addPedido(fechaPedido: String, descripcion: String, detalles: String): Boolean {
        val result = pedidosDAO.addPedido(fechaPedido, descripcion, detalles)
        return result > 0
    }
    fun rememberPedido() {
        // TODO
    }
    fun updatePedido(): Boolean {
        // TODO
        return true
    }

    fun getPedidos(): Flow<List<Pedidos>> {
        return pedidosDAO.getPedidos()
    }
}