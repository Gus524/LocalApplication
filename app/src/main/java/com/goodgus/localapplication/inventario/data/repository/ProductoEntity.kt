package com.goodgus.localapplication.inventario.data.repository

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad de persistencia Room para la tabla Producto.
 * Desacoplada del modelo de dominio puro.
 */
@Entity(tableName = "Producto")
data class ProductoEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_producto")
    val idProducto: Int = 0,
    val nombre: String,
    val marca: String,
    @ColumnInfo(name = "precio_venta")
    val precioVenta: Double,
    val disponibles: Int,
    val tipo: String,
    @ColumnInfo(defaultValue = "1")
    val estado: Int = 1
)