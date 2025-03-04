package com.goodgus.localapplication.models.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Clase para establecer las caracteristicas de nuestros modelos  (tablas) de Room
 * Se establece la primary key y las columnas, estableciendo si pueden ser NULL o sus valores DEFAULT
 * Deben ser igual a las tablas de SQLite (si es que hay una base de datos generada previamente
 *
 * Se establecen los modelos de Cuenta y Venta (CuentaProducto)
 */

@Entity(tableName = "Cuenta")
data class Cuenta (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_cuenta") val idCuenta: Int,
    @ColumnInfo(name = "fecha_cuenta", defaultValue = "(strftime('%Y-%m-%d', 'now'))") val fechaCuenta: String?,
    @ColumnInfo(name = "total_cuenta", defaultValue = "0") val totalCuenta: Double?,
    @ColumnInfo(name = "cuenta_gus", defaultValue = "0") val cuentaGus: Double?,
    @ColumnInfo(name = "cuenta_cele", defaultValue = "0") val cuentaCele: Double?,
    @ColumnInfo(name = "estado_cuenta", defaultValue = "1") val estadoCuenta: Int?

)

@Entity(
    tableName = "Venta",
    foreignKeys = [
        ForeignKey(
            entity = Producto::class,
            parentColumns = ["id_producto"],
            childColumns = ["id_producto"]
        ),
        ForeignKey(
            entity = Cuenta::class,
            parentColumns = ["id_cuenta"],
            childColumns = ["id_cuenta"]
        )
    ],
    indices = [
        Index(value = ["id_producto"]),
        Index(value = ["id_cuenta"])
    ]
)
data class Venta (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_venta") val idVenta: Int? = 0,
    @ColumnInfo(name = "cantidad_producto", defaultValue = "1") val cantidadProducto: Int? = 1,
    @ColumnInfo(name = "hora_venta", defaultValue = "(strftime('%H:%M', 'now'))") val horaVenta: String?,
    @ColumnInfo(name = "parcial_venta") val parcialVenta: Double?,
    @ColumnInfo(name = "estado_venta", defaultValue = "1") val estadoVenta: Int? = 1,
    @ColumnInfo(name = "id_producto") val idProducto: Int = 0,
    @ColumnInfo(name = "id_cuenta") val idCuenta: Int = 0
)