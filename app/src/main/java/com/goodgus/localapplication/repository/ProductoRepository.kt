package com.goodgus.localapplication.repository

import com.goodgus.localapplication.DAO.ProductoDAO
import com.goodgus.localapplication.models.data.Producto

/**
 * Clases de repositorio
 * Son la conexion entre los ViewModel y los DAO de nuestros modelos
 *
 * Implementan los modelos para las funciones y logica de negocio de nuestra aplicacion
 * Los repositorios pueden implementar mas de un DAO
 *
 */


/**
 * Clase ProductoRepository
 *
 * @param productoDAO es la interfaz DAO del modelo Producto
 *
 */

class ProductoRepository(private val productoDAO: ProductoDAO) {
    fun getStorage(): List<Producto> {
        return productoDAO.getAllProducts()
    }

    /**
     * Funcion addProduct implementa addProduct del Dao, agrega un nuevo producto
     *
     * @param nombre nombre del producto
     * @param precioVenta el precio individual por el que se vende cada producto
     * @param disponibles Cantidad disponible del producto
     * @param tipo Tipo del producto
     */

    fun addProduct(nombre: String,
                   marca: String,
                   precioVenta: Double,
                   disponibles: Int,
                   tipo: String): Boolean {
        val result = productoDAO.addProduct(nombre, marca, precioVenta, disponibles, tipo)
        return result > 0
    }

    /**
     * Funcion editProduct implementa updateProducto del Dao, actualiza un producto
     * Para utilizar una sola funcion se mandan los datos completos del producto que se va actualizar
     *
     * @param idProducto ID del producto que se va actualizar
     * @param nombre nombre del producto
     * @param marca La marca del producto
     * @param precioVenta el precio individual por el que se vende cada producto
     * @param disponibles Cantidad disponible del producto
     * @param tipo Tipo del producto
     */

    fun editProduct(
        idProducto: Int,
        nombre: String,
        marca: String,
        precioVenta: Double,
        disponibles: Int,
        tipo: String
    ): Boolean {
        val result = productoDAO.updateProducto(idProducto,nombre,marca,precioVenta,disponibles,tipo)
        return result > 0
    }
    fun downProduct(idProducto: Int): Boolean {
        val result = productoDAO.downProduct(idProducto)
        return result > 0
    }
    fun upProduct(): Boolean {
        // TODO
        return true
    }

    fun getProductId(idProducto: Int): Producto {
        return productoDAO.getProductId(idProducto)
    }

    fun searchProduct(name: String): List<Producto> {
        return productoDAO.searchProduct(name)
    }
}