package com.goodgus.localapplication.DAO

import androidx.room.Dao
import androidx.room.Query
import com.goodgus.localapplication.models.data.Venta
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz de los querys para nuestra tabla Venta
 *
 * Se establecen los querys para obtener las vertas, agregar y actualizar las ventas
 */


@Dao
interface VentaDAO {

    @Query("SELECT * FROM Venta")
    fun getVentas(): Flow<List<Venta>>

    @Query("INSERT INTO Venta(cantidad_producto, id_producto, id_cuenta, parcial_venta) VALUES (:cantidad, :idProducto, :idCuenta, :parcialVenta)")
    fun newVenta(cantidad: Int, idProducto: Int, idCuenta: Int, parcialVenta: Double): Long

    @Query("UPDATE Venta " +
            "SET cantidad_producto = :cantidad, id_producto = :idProducto, parcial_venta = :parcialVenta " +
            "WHERE id_venta = :idVenta")
    fun updateVenta(
        idVenta: Int,
        cantidad: Int,
        idProducto: Int,
        parcialVenta: Double
    ): Int

    @Query("UPDATE Venta SET estado_venta = 0 WHERE id_venta = :idVenta")
    fun removeVenta(idVenta: Int): Int
}