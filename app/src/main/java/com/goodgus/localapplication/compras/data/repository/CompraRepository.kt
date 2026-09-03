package com.goodgus.localapplication.compras.data.repository

import com.goodgus.localapplication.DAO.CompraDAO
import com.goodgus.localapplication.DAO.ProductoDAO
import com.goodgus.localapplication.common.data.repository.BaseRepository
import com.goodgus.localapplication.compras.data.mapper.CompraMapper
import com.goodgus.localapplication.compras.domain.model.Compra
import com.goodgus.localapplication.compras.domain.model.CompraId
import com.goodgus.localapplication.compras.domain.repository.ICompraRepository
import com.goodgus.localapplication.models.data.Compra as CompraEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class CompraRepository @Inject constructor(
    dao: CompraDAO,
    private val productoDao: ProductoDAO,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseRepository<Compra, CompraId, CompraEntity, CompraDAO>(dao, CompraMapper(), ioDispatcher),
    ICompraRepository {

    private val compraMapper = CompraMapper()

    override suspend fun onHydrateQuery(id: CompraId): CompraEntity? {
        return dao.getCompraById(id.valor)
    }

    override suspend fun onGetAllQuery(): List<CompraEntity> {
        return dao.getCompra()
    }

    override suspend fun siguienteId(): CompraId = executeIo {
        CompraId((dao.getMaxId() ?: 0) + 1)
    }.getOrDefault(CompraId(1))

    override suspend fun obtenerPorId(id: CompraId): Compra? = executeIo {
        val entity = onHydrateQuery(id) ?: return@executeIo null
        val items = dao.getItemsByCompraId(id.valor)
        val nombresMap = items.associate { item ->
            val nombre = productoDao?.getProductId(item.idProducto)?.nombre ?: "Producto #${item.idProducto}"
            item.idProducto to nombre
        }
        compraMapper.toDomain(entity, items, nombresMap)
    }.getOrNull()

    override suspend fun guardar(agregado: Compra): Result<Unit> = executeIo {
        val entity = compraMapper.toPersistence(agregado)
        dao.insert(entity)
        if (agregado.productos.isNotEmpty()) {
            val itemEntities = agregado.productos.map {
                compraMapper.toItemPersistence(it, agregado.id)
            }
            dao.insertCompraProductos(itemEntities)
        }
        Unit
    }

    override suspend fun actualizar(agregado: Compra): Result<Unit> = executeIo {
        val entity = compraMapper.toPersistence(agregado)
        val updated = dao.update(entity)
        if (updated == 0) {
            throw NoSuchElementException("No se encontró la compra para actualizar con ID: ${agregado.id}")
        }
        dao.deleteItemsByCompraId(agregado.id.valor)
        if (agregado.productos.isNotEmpty()) {
            val itemEntities = agregado.productos.map {
                compraMapper.toItemPersistence(it, agregado.id)
            }
            dao.insertCompraProductos(itemEntities)
        }
        Unit
    }
}
