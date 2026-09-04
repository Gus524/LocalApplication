package com.goodgus.localapplication.compras.data.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.goodgus.localapplication.core.data.dao.BaseDao

/**
 * Interfaz de los querys para nuestra tabla Compra y sus líneas de detalle
 */
@Dao
interface CompraDAO : BaseDao<Compra> {

    @Query("SELECT * FROM Compra WHERE id_compra = :id")
    fun getById(id: Int): Compra?

    @Query("SELECT * FROM Compra")
    fun getAll(): List<Compra>

    @Query("SELECT MAX(id_compra) FROM Compra")
    fun getMaxId(): Int?

    @Query("SELECT * FROM Compra_Producto WHERE id_compra = :idCompra")
    fun getItemsByCompraId(idCompra: Int): List<CompraProducto>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCompraProductos(items: List<CompraProducto>): List<Long>

    @Query("DELETE FROM Compra_Producto WHERE id_compra = :idCompra")
    fun deleteItemsByCompraId(idCompra: Int): Int
}