package com.goodgus.localapplication.DAO

import androidx.room.Dao
import androidx.room.Query
import com.goodgus.localapplication.models.data.Cuenta
import com.goodgus.localapplication.models.data.Venta
import com.goodgus.localapplication.models.dataView.GetCuenta
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz de los querys para nuestra tabla Cuenta
 *
 * Se establecen los querys para obtener las cuentas, agregar y actualizar las cuentas
 */


@Dao
interface CuentaDAO {
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

    @Query("INSERT INTO Cuenta (estado_cuenta) VALUES (1)")
    fun addAccount() : Long
}