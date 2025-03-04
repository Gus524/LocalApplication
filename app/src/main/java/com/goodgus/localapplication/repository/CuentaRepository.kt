package com.goodgus.localapplication.repository

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import com.goodgus.localapplication.DAO.CuentaDAO
import com.goodgus.localapplication.DAO.ProductoDAO
import com.goodgus.localapplication.DAO.VentaDAO
import com.goodgus.localapplication.models.data.Cuenta
import com.goodgus.localapplication.models.data.Venta
import com.goodgus.localapplication.models.dataView.GetCuenta
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Clases de repositorio
 * Son la conexion entre los ViewModel y los DAO de nuestros modelos
 *
 * Implementan los modelos para las funciones y logica de negocio de nuestra aplicacion
 * Los repositorios pueden implementar mas de un DAO
 *
 */


/**
 * Clase CuentaRepository
 *
 * @param cuentaDAO es la interfaz DAO del modelo Cuenta
 * @param ventaDAO la interfaz del modelo venta
 *
 */

class CuentaRepository(
    private val cuentaDAO: CuentaDAO,
    private val ventaDAO: VentaDAO
) {
    fun addAccount(): Boolean {
        println("llego aqui, creando cuenta")
        val result = cuentaDAO.addAccount()
        return result > 0
    }

    fun getAccountId(): Int {
        return cuentaDAO.getAccountId()
    }
    fun closeAccount(): Boolean {
        val result = cuentaDAO.closeAccount()
        return result > 0
    }
    fun addVenta(cantidad: Int, idCuenta: Int, idProducto: Int, parcialVenta: Double): Boolean {
        try {
            Log.d("VentaDebug", "idProducto: $idProducto, idCuenta: $idCuenta")
            val row = ventaDAO.newVenta(cantidad, idProducto, idCuenta, parcialVenta)

            return row > 0
        } catch (e: SQLiteConstraintException) {
            Log.e("VentaError", "Error al insertar venta: ${e.message}")
            // Maneja el error, por ejemplo, mostrando un mensaje al usuario
            return false
        }
    }

    fun updateVenta(idVenta: Int, cantidad: Int, idProducto: Int, parcialVenta: Double): Boolean {
        val venta = ventaDAO.updateVenta(idVenta, cantidad, idProducto, parcialVenta)
        return venta > 0
    }
    fun removeVenta(idVenta: Int): Boolean {
        val result = ventaDAO.removeVenta(idVenta)
        return (result > 0)
    }
    fun getCuentaActiva(): Flow<List<GetCuenta>> {
        return cuentaDAO.getCuentaActiva()
    }

    fun getCuentaId(idVenta: Int): GetCuenta? {
        return cuentaDAO.getVentaId(idVenta)
    }

    fun getCuentaNoVentas(): List<Cuenta> {
        return cuentaDAO.getCuentaActivaNoVentas()
    }
}