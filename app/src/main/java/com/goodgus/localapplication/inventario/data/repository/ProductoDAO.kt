package com.goodgus.localapplication.inventario.data.repository

import androidx.room.Dao
import androidx.room.Query
import com.goodgus.localapplication.core.data.dao.BaseDao

/**
 * Interfaz de los querys para nuestra tabla Producto
 */
@Dao
interface ProductoDAO : BaseDao<Producto> {

    @Query("SELECT * FROM Producto WHERE id_producto = :id")
    fun getById(id: Int): Producto?

    @Query("SELECT * FROM Producto")
    fun getAll(): List<Producto>

    @Query("SELECT MAX(id_producto) FROM Producto")
    fun getMaxId(): Int?

    @Query("SELECT * FROM Producto WHERE nombre LIKE '%' || :name || '%' OR tipo LIKE '%' || :name || '%' OR marca LIKE '%' || :name || '%'")
    fun searchProduct(name: String): List<Producto>

    @Query("UPDATE Producto SET estado = 0 WHERE id_producto = :idProducto")
    fun downProduct(idProducto: Int): Int
}