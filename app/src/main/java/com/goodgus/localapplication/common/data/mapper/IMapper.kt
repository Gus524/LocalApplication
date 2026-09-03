package com.goodgus.localapplication.common.data.mapper

/**
 * Contrato puro 1 a 1 para transformaciones entre Dominio y Persistencia.
 */
interface IMapper<TDomain, TPersistence> {
    fun toDomain(entity: TPersistence): TDomain
    fun toPersistence(domain: TDomain): TPersistence
}
