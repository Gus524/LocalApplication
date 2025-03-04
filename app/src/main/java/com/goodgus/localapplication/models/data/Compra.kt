package com.goodgus.localapplication.models.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Clase para establecer las caracteristicas de nuestros modelos  (tablas) de Room
 * Se establece la primary key y las columnas, estableciendo si pueden ser NULL o sus valores DEFAULT
 * Deben ser igual a las tablas de SQLite (si es que hay una base de datos generada previamente
 *
 * Se establecen los modelos de Compra y CompraProducto
 */


@Entity(tableName = "Compra")
class Compra (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_compra") val idCompra: Int,
    @ColumnInfo(name = "total_compra", defaultValue = "0") val totalCompra: Double?,
    @ColumnInfo(name = "fecha_compra", defaultValue = "(strftime('%Y-%m-%d', 'now'))") val fechaCompra: String?
)

@Entity(
    tableName = "Compra_Producto",
    foreignKeys = [
        ForeignKey(
            entity = Compra::class,
            parentColumns = ["id_compra"],
            childColumns = ["id_compra"]
        ),
        ForeignKey(
            entity = Producto::class,
            parentColumns = ["id_producto"],
            childColumns = ["id_producto"]
        )
    ],
    indices = [
        Index(value = ["id_compra"]),
        Index(value = ["id_producto"])
    ]
)
class CompraProducto (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_compra_producto") val idCompraProducto: Int?,
    @ColumnInfo(name = "parcial_compra") val parcialCompra: Double,
    @ColumnInfo(name = "cantidad_producto", defaultValue = "1") val cantidadProducto: Int?,
    @ColumnInfo(name = "id_compra") val idCompra: Int,
    @ColumnInfo(name = "id_producto") val idProducto: Int
)