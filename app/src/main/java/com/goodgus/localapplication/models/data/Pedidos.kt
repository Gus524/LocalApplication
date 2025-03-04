package com.goodgus.localapplication.models.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Clase para establecer las caracteristicas de nuestros modelos  (tablas) de Room
 * Se establece la primary key y las columnas, estableciendo si pueden ser NULL o sus valores DEFAULT
 * Deben ser igual a las tablas de SQLite (si es que hay una base de datos generada previamente
 *
 * Se establece el modelo de pedidos
 */

@Entity(tableName = "Pedidos")
data class Pedidos (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_pedido") val idPedido: Int?,
    @ColumnInfo(name = "fecha_pedido") val fechaPedido: String,
    @ColumnInfo(name = "fecha_entrega") val fechaEntrega: String?,
    val descripcion: String,
    val detalles: String?
)