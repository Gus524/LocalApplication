package com.goodgus.localapplication.pedidos.domain.repository

import com.goodgus.localapplication.common.domain.IRepository
import com.goodgus.localapplication.pedidos.domain.model.Pedido
import com.goodgus.localapplication.pedidos.domain.model.PedidoId

/**
 * Contrato de repositorio puro para el Agregado Pedido.
 */
interface IPedidoRepository : IRepository<Pedido, PedidoId>
