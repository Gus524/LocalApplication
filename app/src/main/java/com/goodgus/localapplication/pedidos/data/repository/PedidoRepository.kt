package com.goodgus.localapplication.pedidos.data.repository

import com.goodgus.localapplication.DAO.PedidosDAO
import com.goodgus.localapplication.common.data.mapper.IMapper
import com.goodgus.localapplication.common.data.repository.BaseRepository
import com.goodgus.localapplication.models.data.Pedidos as PedidosEntity
import com.goodgus.localapplication.pedidos.data.mapper.PedidoMapper
import com.goodgus.localapplication.pedidos.domain.model.Pedido
import com.goodgus.localapplication.pedidos.domain.model.PedidoId
import com.goodgus.localapplication.pedidos.domain.repository.IPedidoRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class PedidoRepository(
    dao: PedidosDAO,
    mapper: IMapper<Pedido, PedidosEntity> = PedidoMapper(),
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseRepository<Pedido, PedidoId, PedidosEntity, PedidosDAO>(dao, mapper, ioDispatcher),
    IPedidoRepository {

    override suspend fun onHydrateQuery(id: PedidoId): PedidosEntity? {
        return dao.getPedidoById(id.valor)
    }

    override suspend fun onGetAllQuery(): List<PedidosEntity> {
        return dao.getAllPedidos()
    }

    override suspend fun siguienteId(): PedidoId = executeIo {
        PedidoId((dao.getMaxId() ?: 0) + 1)
    }.getOrDefault(PedidoId(1))
}
