package com.goodgus.localapplication.core.data.dao

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.goodgus.localapplication.compras.data.repository.CompraDAO
import com.goodgus.localapplication.compras.data.repository.CompraProductoDAO
import com.goodgus.localapplication.ventas.data.repository.CuentaDAO
import com.goodgus.localapplication.pedidos.data.repository.PedidosDAO
import com.goodgus.localapplication.inventario.data.repository.ProductoDAO
import com.goodgus.localapplication.ventas.data.repository.VentaDAO
import com.goodgus.localapplication.compras.data.repository.Compra
import com.goodgus.localapplication.compras.data.repository.CompraProducto
import com.goodgus.localapplication.ventas.data.repository.Cuenta
import com.goodgus.localapplication.pedidos.data.repository.Pedidos
import com.goodgus.localapplication.inventario.data.repository.Producto
import com.goodgus.localapplication.ventas.data.repository.Venta
import com.goodgus.localapplication.ventas.data.repository.GetCuenta

/**
 * Objeto para establecer la estructura de la base de datos, se declaran las tablas, las vistas y la version
 */

@Database(
    entities = [Producto::class, Venta::class, Cuenta::class, Pedidos::class, Compra::class, CompraProducto::class],
    views = [GetCuenta::class],
    version = 1,
    exportSchema = false
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