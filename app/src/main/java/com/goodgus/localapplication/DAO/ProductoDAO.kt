package com.goodgus.localapplication.DAO

import androidx.room.Dao
import androidx.room.Query
import com.goodgus.localapplication.models.data.Producto

/**
 * Interfaz de los querys para nuestra tabla Producto
 *
 * Se establecen los querys para obtener los productos por diferentes parametros y agregar y actualizar productos
 */


@Dao
interface ProductoDAO {

    @Query("SELECT * FROM Producto")
    fun getAllProducts(): List<Producto>

    @Query("SELECT * FROM Producto WHERE nombre LIKE '%' || :name || '%' OR tipo LIKE '%' || :name || '%' OR marca LIKE '%' || :name || '%'")
    fun searchProduct(name: String): List<Producto>

    @Query("SELECT * FROM Producto WHERE id_producto = :idProducto")
    fun getProductId(idProducto: Int): Producto

    @Query("UPDATE Producto SET estado = 0 WHERE id_producto = :idProducto")
    fun downProduct(idProducto: Int): Int

    @Query("INSERT OR REPLACE INTO Producto(nombre, marca, precio_venta, disponibles, tipo) " +
                                    "VALUES (:nombre, :marca, :precioVenta, :disponibles, :tipo)")
    fun addProduct(
        nombre: String,
        marca: String,
        precioVenta: Double,
        disponibles: Int,
        tipo: String
    ): Long

    @Query("INSERT OR REPLACE INTO Producto(id_producto, nombre, marca, precio_venta, disponibles, tipo) " +
                                     "VALUES (:idProducto, :nombre, :marca, :precioVenta, :disponibles, :tipo)")
    fun updateProducto(
        idProducto: Int,
        nombre: String,
        marca: String,
        precioVenta: Double,
        disponibles: Int,
        tipo: String
    ) : Long
}