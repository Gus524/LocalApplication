package com.goodgus.localapplication.ventas.data.repository

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.goodgus.localapplication.inventario.data.repository.Producto

/**
 * Entidades de persistencia Room para las tablas Cuenta y Venta.
 * Desacopladas de los agregados y entidades de dominio puro.
 */

@Entity(tableName = "Cuenta")
data class Cuenta(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_cuenta")
    val idCuenta: Int = 0,
    @ColumnInfo(name = "fecha_cuenta", defaultValue = "(strftime('%Y-%m-%d', 'now'))")
    val fechaCuenta: String = "",
    @ColumnInfo(name = "total_cuenta", defaultValue = "0")
    val totalCuenta: Double = 0.0,
    @ColumnInfo(name = "cuenta_gus", defaultValue = "0")
    val cuentaGus: Double = 0.0,
    @ColumnInfo(name = "cuenta_cele", defaultValue = "0")
    val cuentaCele: Double = 0.0,
    @ColumnInfo(name = "estado_cuenta", defaultValue = "1")
    val estadoCuenta: Int = 1
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
data class Venta(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_venta")
    val idVenta: Int = 0,
    @ColumnInfo(name = "cantidad_producto", defaultValue = "1")
    val cantidadProducto: Int = 1,
    @ColumnInfo(name = "hora_venta", defaultValue = "(strftime('%H:%M', 'now'))")
    val horaVenta: String = "",
    @ColumnInfo(name = "parcial_venta")
    val parcialVenta: Double = 0.0,
    @ColumnInfo(name = "estado_venta", defaultValue = "1")
    val estadoVenta: Int = 1,
    @ColumnInfo(name = "id_producto")
    val idProducto: Int = 0,
    @ColumnInfo(name = "id_cuenta")
    val idCuenta: Int = 0
)