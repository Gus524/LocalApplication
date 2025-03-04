package com.goodgus.localapplication.DAO

import androidx.room.Dao
import androidx.room.Query
import com.goodgus.localapplication.models.data.CompraProducto
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz de los querys para nuestra tabla CompraProducto
 */


@Dao
interface CompraProductoDAO {
    @Query("SELECT * FROM Compra_Producto")
    fun getCompraProducto(): Flow<List<CompraProducto>>

    @Query("SELECT * FROM Compra_Producto WHERE id_compra_producto = :idCompra")
    fun getCompraId(idCompra: Int): List<CompraProducto>
}