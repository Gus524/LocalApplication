package com.goodgus.localapplication.DAO

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.goodgus.localapplication.models.data.Compra
import com.goodgus.localapplication.models.data.CompraProducto
import com.goodgus.localapplication.models.data.Cuenta
import com.goodgus.localapplication.models.data.Pedidos
import com.goodgus.localapplication.models.data.Producto
import com.goodgus.localapplication.models.data.Venta
import com.goodgus.localapplication.models.dataView.GetCuenta
import com.goodgus.localapplication.utilidades.Converters

/**
 * Objeto para establecer la estructura de la base de datos, se declaran las tablas, las vistas y la version
 */

@Database(
    entities = [Producto::class, Venta::class, Cuenta::class, Pedidos::class, Compra::class, CompraProducto::class],
    views = [GetCuenta::class],
    version = 1,
    exportSchema = false
)

@TypeConverters(
    Converters::class
)

/**
 * Clase abstracta para la creacion con patron Singleton de la base de datos con Room
 */

abstract class AppDataBase: RoomDatabase() {
    abstract fun productoDao(): ProductoDAO
    abstract fun ventaDao(): VentaDAO
    abstract fun cuentaDao(): CuentaDAO
    abstract fun pedidosDAO(): PedidosDAO
    abstract fun compraDAO(): CompraDAO
    abstract fun compraProductoDAO(): CompraProductoDAO

    companion object {
        @Volatile
        private var INSTANCE: AppDataBase? = null

        fun getInstance(context: Context): AppDataBase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDataBase::class.java,
                    "db_local.db",
                )
                    .createFromAsset(databaseFilePath = "database/db_local.db")
                    .fallbackToDestructiveMigrationFrom(6)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}