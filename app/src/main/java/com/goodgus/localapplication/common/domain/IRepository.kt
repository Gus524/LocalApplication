package com.goodgus.localapplication.common.domain

/**
 * Contrato genérico de persistencia.
 * Restringe la existencia de repositorios únicamente a raíces de agregado.
 */
interface IRepository<TAggregate : AggregateRoot<TId>, TId> {
    suspend fun obtenerPorId(id: TId): TAggregate?
    suspend fun guardar(agregado: TAggregate): Result<Unit>
    suspend fun actualizar(agregado: TAggregate): Result<Unit>
    suspend fun eliminar(id: TId): Result<Unit>
}
