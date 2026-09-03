package com.goodgus.localapplication.shared.di

import com.goodgus.localapplication.compras.data.repository.CompraRepository
import com.goodgus.localapplication.compras.domain.repository.ICompraRepository
import com.goodgus.localapplication.inventario.data.repository.ProductoRepository
import com.goodgus.localapplication.inventario.domain.repository.IProductoRepository
import com.goodgus.localapplication.pedidos.data.repository.PedidoRepository
import com.goodgus.localapplication.pedidos.domain.repository.IPedidoRepository
import com.goodgus.localapplication.ventas.data.repository.CuentaRepository
import com.goodgus.localapplication.ventas.domain.repository.ICuentaRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindProductoRepository(impl: ProductoRepository): IProductoRepository

    @Binds
    @Singleton
    abstract fun bindPedidoRepository(impl: PedidoRepository): IPedidoRepository

    @Binds
    @Singleton
    abstract fun bindCompraRepository(impl: CompraRepository): ICompraRepository

    @Binds
    @Singleton
    abstract fun bindCuentaRepository(impl: CuentaRepository): ICuentaRepository
}
