package com.goodgus.localapplication.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.goodgus.localapplication.models.data.Compra
import com.goodgus.localapplication.models.data.CompraProducto

/**
 * Interfaz de los querys para nuestra tabla Compra y sus líneas de detalle
 */

@Dao
interface CompraDAO : BaseDao<Compra> {
    @Query("SELECT * FROM Compra")
    fun getCompra(): List<Compra>

    @Query("SELECT * FROM Compra WHERE id_compra = :idCompra")
    fun getCompraById(idCompra: Int): Compra?

    @Query("SELECT MAX(id_compra) FROM Compra")
    fun getMaxId(): Int?

    @Query("SELECT * FROM Compra_Producto WHERE id_compra = :idCompra")
    fun getItemsByCompraId(idCompra: Int): List<CompraProducto>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCompraProductos(items: List<CompraProducto>)

    @Query("DELETE FROM Compra_Producto WHERE id_compra = :idCompra")
    fun deleteItemsByCompraId(idCompra: Int): Int
}