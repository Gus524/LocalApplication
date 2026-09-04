package com.goodgus.localapplication.compras.data.repository

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.goodgus.localapplication.inventario.data.repository.ProductoEntity

/**
 * Entidades de persistencia Room para las tablas Compra y Compra_Producto.
 * Desacopladas de los agregados y entidades de dominio puro.
 */

@Entity(tableName = "Compra")
data class CompraEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_compra")
    val idCompra: Int = 0,
    @ColumnInfo(name = "total_compra", defaultValue = "0")
    val totalCompra: Double = 0.0,
    @ColumnInfo(name = "fecha_compra", defaultValue = "(strftime('%Y-%m-%d', 'now'))")
    val fechaCompra: String = ""
)

@Entity(
    tableName = "Compra_Producto",
    foreignKeys = [
        ForeignKey(
            entity = CompraEntity::class,
            parentColumns = ["id_compra"],
            childColumns = ["id_compra"]
        ),
        ForeignKey(
            entity = ProductoEntity::class,
            parentColumns = ["id_producto"],
            childColumns = ["id_producto"]
        )
    ],
    indices = [
        Index(value = ["id_compra"]),
        Index(value = ["id_producto"])
    ]
)
data class CompraProductoEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_compra_producto")
    val idCompraProducto: Int = 0,
    @ColumnInfo(name = "parcial_compra")
    val parcialCompra: Double = 0.0,
    @ColumnInfo(name = "cantidad_producto", defaultValue = "1")
    val cantidadProducto: Int = 1,
    @ColumnInfo(name = "id_compra")
    val idCompra: Int = 0,
    @ColumnInfo(name = "id_producto")
    val idProducto: Int = 0
)