package com.goodgus.localapplication.compras.data.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.goodgus.localapplication.core.data.dao.BaseDao
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz de los querys para nuestra tabla Compra y sus líneas de detalle
 */
@Dao
interface CompraDAO : BaseDao<CompraEntity> {

    @Query("SELECT * FROM Compra WHERE id_compra = :id")
    fun getById(id: Int): CompraEntity?

    @Query("SELECT * FROM Compra")
    fun getAll(): List<CompraEntity>

    @Query("SELECT * FROM Compra")
    fun observarTodos(): Flow<List<CompraEntity>>

    @Query("SELECT MAX(id_compra) FROM Compra")
    fun getMaxId(): Int?

    @Query("SELECT * FROM Compra_Producto WHERE id_compra = :idCompra")
    fun getItemsByCompraId(idCompra: Int): List<CompraProductoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCompraProductos(items: List<CompraProductoEntity>): List<Long>

    @Query("DELETE FROM Compra_Producto WHERE id_compra = :idCompra")
    fun deleteItemsByCompraId(idCompra: Int): Int
}