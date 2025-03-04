package com.goodgus.localapplication.repository

import com.goodgus.localapplication.DAO.CompraDAO
import com.goodgus.localapplication.DAO.CompraProductoDAO
import com.goodgus.localapplication.models.data.Compra
import com.goodgus.localapplication.models.data.CompraProducto
import com.goodgus.localapplication.models.data.Cuenta

/**
 * Clases de repositorio
 * Son la conexion entre los ViewModel y los DAO de nuestros modelos
 *
 * Implementan los modelos para las funciones y logica de negocio de nuestra aplicacion
 * Los repositorios pueden implementar mas de un DAO
 *
 */


/**
 * Clase CompraRepository
 *
 * @param compraDAO es la interfaz DAO del modelo Compra
 * @param compraProductoDAO la interfaz del modelo compraProducto
 *
 */

class CompraRepository(
    private val compraDAO: CompraDAO,
    private val compraProductoDAO: CompraProductoDAO
) {
    fun getCompraId(idCompra: Int): List<CompraProducto>{
        return compraProductoDAO.getCompraId(idCompra)
    }

    fun openCompra(): Boolean {
        // TODO
        return true
    }

    fun getCompras(): List<Compra> {
        return compraDAO.getCompra()
    }
    fun closeCompra(): Boolean {
        // TODO
        return true
    }
    fun addCompra(): Boolean {
        // TODO
        return true
    }

    fun updateCompra(): Boolean {
        // TODO
        return true
    }
    fun dropCompra(): Boolean {
        // TODO
        return true
    }

    fun getAllCompras(): List<Compra>? {
        // TODO
        return null
    }
}