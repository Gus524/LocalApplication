package com.goodgus.localapplication.inventario.data.repository

import com.goodgus.localapplication.core.data.repository.BaseRepository
import com.goodgus.localapplication.inventario.data.mapper.ProductoMapper
import com.goodgus.localapplication.inventario.domain.model.Producto
import com.goodgus.localapplication.inventario.domain.model.ProductoId
import com.goodgus.localapplication.inventario.domain.repository.IProductoRepository
import com.goodgus.localapplication.inventario.data.repository.Producto as ProductoEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class ProductoRepository @Inject constructor(
    dao: ProductoDAO,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseRepository<Producto, ProductoId, ProductoEntity, ProductoDAO>(dao, ProductoMapper(), ioDispatcher),
    IProductoRepository {

    override suspend fun onHydrateQuery(id: ProductoId): ProductoEntity? {
        return dao.getById(id.valor)
    }

    override suspend fun onGetAllQuery(): List<ProductoEntity> {
        return dao.getAll()
    }

    override suspend fun siguienteId(): ProductoId = executeIo {
        ProductoId((dao.getMaxId() ?: 0) + 1)
    }.getOrDefault(ProductoId(1))

    override suspend fun buscarPorCriterio(criterio: String): List<Producto> = executeIo {
        toDomainList(dao.searchProduct(criterio))
    }.getOrDefault(emptyList())
}
