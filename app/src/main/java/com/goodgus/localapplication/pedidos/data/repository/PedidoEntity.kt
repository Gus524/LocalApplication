package com.goodgus.localapplication.pedidos.data.repository

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad de persistencia Room para la tabla Pedidos.
 * Desacoplada del modelo de dominio puro.
 */
@Entity(tableName = "Pedidos")
data class PedidoEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_pedido")
    val idPedido: Int = 0,
    @ColumnInfo(name = "fecha_pedido")
    val fechaPedido: String = "",
    @ColumnInfo(name = "fecha_entrega")
    val fechaEntrega: String? = null,
    val descripcion: String = "",
    val detalles: String? = null
)