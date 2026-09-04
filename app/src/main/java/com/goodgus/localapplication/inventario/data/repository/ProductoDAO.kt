package com.goodgus.localapplication.inventario.data.repository

import androidx.room.Dao
import androidx.room.Query
import com.goodgus.localapplication.core.data.dao.BaseDao
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz de los querys para nuestra tabla Producto
 */
@Dao
interface ProductoDAO : BaseDao<ProductoEntity> {

    @Query("SELECT * FROM Producto WHERE id_producto = :id")
    fun getById(id: Int): ProductoEntity?

    @Query("SELECT * FROM Producto")
    fun getAll(): List<ProductoEntity>

    @Query("SELECT * FROM Producto")
    fun observarTodos(): Flow<List<ProductoEntity>>

    @Query("SELECT MAX(id_producto) FROM Producto")
    fun getMaxId(): Int?

    @Query("SELECT * FROM Producto WHERE nombre LIKE '%' || :name || '%' OR tipo LIKE '%' || :name || '%' OR marca LIKE '%' || :name || '%'")
    fun searchProduct(name: String): List<ProductoEntity>

    @Query("SELECT * FROM Producto WHERE nombre LIKE '%' || :name || '%' OR tipo LIKE '%' || :name || '%' OR marca LIKE '%' || :name || '%'")
    fun observarSearchProduct(name: String): Flow<List<ProductoEntity>>

    @Query("UPDATE Producto SET estado = 0 WHERE id_producto = :idProducto")
    fun downProduct(idProducto: Int): Int
}