package com.goodgus.localapplication.ventas.data.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.goodgus.localapplication.core.data.dao.BaseDao
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz de los querys para nuestra tabla Cuenta y sus líneas de venta
 */
@Dao
interface CuentaDAO : BaseDao<Cuenta> {

    @Query("SELECT * FROM Cuenta WHERE id_cuenta = :id")
    fun getById(id: Int): Cuenta?

    @Query("SELECT * FROM Cuenta")
    fun getAll(): List<Cuenta>

    @Query("SELECT MAX(id_cuenta) FROM Cuenta")
    fun getMaxId(): Int?

    @Query("SELECT * FROM Cuenta")
    fun getCuenta(): Flow<List<Cuenta>>

    @Query("SELECT * FROM GetCuenta WHERE estado_cuenta = 1 AND estado_venta = 1 ORDER BY id_venta DESC")
    fun getCuentaActiva(): Flow<List<GetCuenta>>

    @Query("SELECT * FROM Cuenta WHERE estado_cuenta = 1")
    fun getCuentaActivaNoVentas(): List<Cuenta>

    @Query("SELECT * FROM GetCuenta WHERE id_venta = :idVenta")
    fun getVentaId(idVenta: Int): GetCuenta?

    @Query("UPDATE Cuenta SET estado_cuenta = 0")
    fun closeAccount(): Int

    @Query("SELECT id_cuenta FROM Cuenta WHERE estado_cuenta = 1 LIMIT 1")
    fun getAccountId(): Int

    @Query("SELECT * FROM Venta WHERE id_cuenta = :idCuenta")
    fun getVentasByCuentaId(idCuenta: Int): List<Venta>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertVentas(ventas: List<Venta>): List<Long>

    @Query("DELETE FROM Venta WHERE id_cuenta = :idCuenta")
    fun deleteVentasByCuentaId(idCuenta: Int): Int
}