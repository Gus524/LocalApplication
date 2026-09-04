package com.goodgus.localapplication.core.data.dao

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.goodgus.localapplication.compras.data.repository.CompraDAO
import com.goodgus.localapplication.ventas.data.repository.CuentaDAO
import com.goodgus.localapplication.pedidos.data.repository.PedidosDAO
import com.goodgus.localapplication.inventario.data.repository.ProductoDAO
import com.goodgus.localapplication.compras.data.repository.CompraEntity
import com.goodgus.localapplication.compras.data.repository.CompraProductoEntity
import com.goodgus.localapplication.ventas.data.repository.CuentaEntity
import com.goodgus.localapplication.pedidos.data.repository.PedidoEntity
import com.goodgus.localapplication.inventario.data.repository.ProductoEntity
import com.goodgus.localapplication.ventas.data.repository.VentaEntity
import com.goodgus.localapplication.ventas.data.repository.GetCuenta

/**
 * Objeto para establecer la estructura de la base de datos, se declaran las tablas, las vistas y la version
 */

@Database(
    entities = [ProductoEntity::class, VentaEntity::class, CuentaEntity::class, PedidoEntity::class, CompraEntity::class, CompraProductoEntity::class],
    views = [GetCuenta::class],
    version = 1,
    exportSchema = false
)

/**
 * Clase abstracta para la creacion con patron Singleton de la base de datos con Room
 */

abstract class AppDataBase: RoomDatabase() {
    abstract fun productoDao(): ProductoDAO
    abstract fun cuentaDao(): CuentaDAO
    abstract fun pedidosDAO(): PedidosDAO
    abstract fun compraDAO(): CompraDAO

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
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}