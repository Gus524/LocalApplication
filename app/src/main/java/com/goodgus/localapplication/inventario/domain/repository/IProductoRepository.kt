package com.goodgus.localapplication.inventario.domain.repository

import com.goodgus.localapplication.common.domain.IRepository
import com.goodgus.localapplication.inventario.domain.model.Producto
import com.goodgus.localapplication.inventario.domain.model.ProductoId

/**
 * Contrato de repositorio exclusivo para el Agregado Producto.
 */
interface IProductoRepository : IRepository<Producto, ProductoId> {
    suspend fun obtenerTodos(): List<Producto>
    suspend fun buscarPorCriterio(criterio: String): List<Producto>
}
