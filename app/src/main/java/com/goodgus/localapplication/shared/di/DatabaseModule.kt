package com.goodgus.localapplication.shared.di

import android.content.Context
import com.goodgus.localapplication.DAO.AppDataBase
import com.goodgus.localapplication.DAO.CompraDAO
import com.goodgus.localapplication.DAO.CompraProductoDAO
import com.goodgus.localapplication.DAO.CuentaDAO
import com.goodgus.localapplication.DAO.PedidosDAO
import com.goodgus.localapplication.DAO.ProductoDAO
import com.goodgus.localapplication.DAO.VentaDAO
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDataBase {
        return AppDataBase.getInstance(context)
    }

    @Provides
    fun provideProductoDAO(db: AppDataBase): ProductoDAO = db.productoDao()

    @Provides
    fun providePedidosDAO(db: AppDataBase): PedidosDAO = db.pedidosDAO()

    @Provides
    fun provideCompraDAO(db: AppDataBase): CompraDAO = db.compraDAO()

    @Provides
    fun provideCompraProductoDAO(db: AppDataBase): CompraProductoDAO = db.compraProductoDAO()

    @Provides
    fun provideCuentaDAO(db: AppDataBase): CuentaDAO = db.cuentaDao()

    @Provides
    fun provideVentaDAO(db: AppDataBase): VentaDAO = db.ventaDao()
}
