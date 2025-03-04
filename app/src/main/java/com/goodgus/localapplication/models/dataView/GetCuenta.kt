package com.goodgus.localapplication.models.dataView

import androidx.room.ColumnInfo
import androidx.room.DatabaseView

import java.util.Date

/**
 * Clase para establecer la creacion y consulta de la vista (VIEW) GetCuenta, contiene la informacion de las cuentas junto con los productos de esa cuenta
 */

@DatabaseView("SELECT  c.id_cuenta," +
        "                c.fecha_cuenta," +
        "                c.total_cuenta," +
        "                c.estado_cuenta, c.cuenta_cele, c.cuenta_gus," +
        "                p.id_producto, p.nombre," +
        "                p.marca," +
        "                p.precio_venta," +
        "                p.tipo," +
        "                v.id_venta," +
        "                v.cantidad_producto," +
        "                v.hora_venta," +
        "                v.parcial_venta, estado_venta" +
        "        FROM" +
        "                Producto p" +
        "        JOIN" +
        "                Venta v" +
        "        ON" +
        "                v.id_producto = p.id_producto" +
        "        JOIN" +
        "                Cuenta c" +
        "        ON" +
        "                c.id_cuenta = v.id_cuenta",
    viewName = "GetCuenta")
data class GetCuenta(
    @ColumnInfo(name = "id_cuenta") val idCuenta: Int,
    @ColumnInfo(name = "fecha_cuenta") val fechaCuenta: Date,
    @ColumnInfo(name = "total_cuenta") val totalCuenta: Double,
    @ColumnInfo(name = "estado_cuenta") val estadoCuenta: Int,
    @ColumnInfo(name = "cuenta_cele") val cuentaCele: Double,
    @ColumnInfo(name = "cuenta_gus") val cuentaGus: Double,
    val nombre: String,
    val marca: String,
    @ColumnInfo(name = "precio_venta") val precioVenta: Double,
    @ColumnInfo(name = "id_producto") val idProducto: Int,
    val tipo: String,
    @ColumnInfo(name = "id_venta") val idVenta: Int,
    @ColumnInfo(name = "cantidad_producto") val cantidadProducto: Int,
    @ColumnInfo(name = "hora_venta") val horaVenta: String,
    @ColumnInfo(name = "parcial_venta") val parcialVenta: Double,
    @ColumnInfo(name = "estado_venta") val estadoVenta: Int
)