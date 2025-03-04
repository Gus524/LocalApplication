package com.goodgus.localapplication.models.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Clase para establecer las caracteristicas de nuestros modelos  (tablas) de Room
 * Se establece la primary key y las columnas, estableciendo si pueden ser NULL o sus valores DEFAULT
 * Deben ser igual a las tablas de SQLite (si es que hay una base de datos generada previamente
 *
 * Se establecen el modelo de Producto
 */

@Entity(tableName = "Producto")
class Producto (
    @ColumnInfo(name = "id_producto")
    @PrimaryKey(autoGenerate = true)
    val idProducto: Int,
    val nombre: String,
    val marca: String,
    @ColumnInfo(name = "precio_venta")
    val precioVenta: Double,
    val disponibles: Int,
    val tipo: String,
    @ColumnInfo(defaultValue = "1") val estado: Int?
)