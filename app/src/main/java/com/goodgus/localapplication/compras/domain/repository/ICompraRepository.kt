package com.goodgus.localapplication.compras.domain.repository

import com.goodgus.localapplication.common.domain.IRepository
import com.goodgus.localapplication.compras.domain.model.Compra
import com.goodgus.localapplication.compras.domain.model.CompraId

/**
 * Contrato de repositorio puro para el Agregado Compra.
 */
interface ICompraRepository : IRepository<Compra, CompraId>
