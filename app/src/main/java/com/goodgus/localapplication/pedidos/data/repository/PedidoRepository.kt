package com.goodgus.localapplication.pedidos.data.repository

import com.goodgus.localapplication.core.data.repository.BaseRepository
import com.goodgus.localapplication.pedidos.data.repository.Pedidos as PedidosEntity
import com.goodgus.localapplication.pedidos.data.mapper.PedidoMapper
import com.goodgus.localapplication.pedidos.domain.model.Pedido
import com.goodgus.localapplication.pedidos.domain.model.PedidoId
import com.goodgus.localapplication.pedidos.domain.repository.IPedidoRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class PedidoRepository @Inject constructor(
    dao: PedidosDAO,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseRepository<Pedido, PedidoId, PedidosEntity, PedidosDAO>(dao, PedidoMapper(), ioDispatcher),
    IPedidoRepository {

    override suspend fun onHydrateQuery(id: PedidoId): PedidosEntity? {
        return dao.getById(id.valor)
    }

    override suspend fun onGetAllQuery(): List<PedidosEntity> {
        return dao.getAll()
    }

    override suspend fun siguienteId(): PedidoId = executeIo {
        PedidoId((dao.getMaxId() ?: 0) + 1)
    }.getOrDefault(PedidoId(1))
}
