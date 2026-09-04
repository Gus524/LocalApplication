package com.goodgus.localapplication.ventas.data.repository

import androidx.room.ColumnInfo
import androidx.room.DatabaseView

/**
 * Creación y consulta de la vista (VIEW) GetCuenta de Room.
 * Contiene la información consolidada de las cuentas junto con las líneas de venta.
 */
@DatabaseView(
    "SELECT  c.id_cuenta," +
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
    viewName = "GetCuenta"
)
data class GetCuenta(
    @ColumnInfo(name = "id_cuenta") val idCuenta: Int = 0,
    @ColumnInfo(name = "fecha_cuenta") val fechaCuenta: String = "",
    @ColumnInfo(name = "total_cuenta") val totalCuenta: Double = 0.0,
    @ColumnInfo(name = "estado_cuenta") val estadoCuenta: Int = 1,
    @ColumnInfo(name = "cuenta_cele") val cuentaCele: Double = 0.0,
    @ColumnInfo(name = "cuenta_gus") val cuentaGus: Double = 0.0,
    val nombre: String = "",
    val marca: String = "",
    @ColumnInfo(name = "precio_venta") val precioVenta: Double = 0.0,
    @ColumnInfo(name = "id_producto") val idProducto: Int = 0,
    val tipo: String = "",
    @ColumnInfo(name = "id_venta") val idVenta: Int = 0,
    @ColumnInfo(name = "cantidad_producto") val cantidadProducto: Int = 1,
    @ColumnInfo(name = "hora_venta") val horaVenta: String = "",
    @ColumnInfo(name = "parcial_venta") val parcialVenta: Double = 0.0,
    @ColumnInfo(name = "estado_venta") val estadoVenta: Int = 1
)