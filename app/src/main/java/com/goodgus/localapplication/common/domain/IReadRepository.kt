package com.goodgus.localapplication.common.domain

/**
 * Contrato genérico de sólo lectura (ISP) para operaciones de consulta y lectura de Agregados.
 * Es la abstracción de repositorio que los ViewModels pueden inyectar para operaciones Query.
 */
interface IReadRepository<TAggregate : AggregateRoot<TId>, TId> {
    suspend fun obtenerPorId(id: TId): TAggregate?
    suspend fun obtenerTodos(): List<TAggregate>
}
