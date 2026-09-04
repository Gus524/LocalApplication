package com.goodgus.localapplication.compras.data.repository

import androidx.room.Dao
import androidx.room.Query
import com.goodgus.localapplication.core.data.dao.BaseDao
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz de los querys para nuestra tabla CompraProducto
 */
@Dao
interface CompraProductoDAO : BaseDao<CompraProducto> {

    @Query("SELECT * FROM Compra_Producto WHERE id_compra_producto = :id")
    fun getById(id: Int): CompraProducto?

    @Query("SELECT * FROM Compra_Producto")
    fun getAll(): List<CompraProducto>

    @Query("SELECT MAX(id_compra_producto) FROM Compra_Producto")
    fun getMaxId(): Int?

    @Query("SELECT * FROM Compra_Producto")
    fun getCompraProducto(): Flow<List<CompraProducto>>

    @Query("SELECT * FROM Compra_Producto WHERE id_compra = :idCompra")
    fun getItemsByCompraId(idCompra: Int): List<CompraProducto>
}