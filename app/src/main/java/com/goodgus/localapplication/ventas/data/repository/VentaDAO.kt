package com.goodgus.localapplication.ventas.data.repository

import androidx.room.Dao
import androidx.room.Query
import com.goodgus.localapplication.core.data.dao.BaseDao
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz de los querys para nuestra tabla Venta
 */
@Dao
interface VentaDAO : BaseDao<Venta> {

    @Query("SELECT * FROM Venta WHERE id_venta = :id")
    fun getById(id: Int): Venta?

    @Query("SELECT * FROM Venta")
    fun getAll(): List<Venta>

    @Query("SELECT MAX(id_venta) FROM Venta")
    fun getMaxId(): Int?

    @Query("SELECT * FROM Venta")
    fun getVentas(): Flow<List<Venta>>

    @Query("SELECT * FROM Venta WHERE id_cuenta = :idCuenta")
    fun getVentasByCuentaId(idCuenta: Int): List<Venta>

    @Query("UPDATE Venta SET estado_venta = 0 WHERE id_venta = :idVenta")
    fun removeVenta(idVenta: Int): Int
}