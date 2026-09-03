package com.goodgus.localapplication.core.domain

/**
 * Contrato genérico de persistencia y ciclo de vida de Agregados.
 * Restringe la existencia de repositorios únicamente a raíces de agregado.
 * Hereda las operaciones de sólo lectura de IReadRepository e incorpora mutaciones y despacho de IDs.
 */
interface IRepository<TAggregate : AggregateRoot<TId>, TId> : IReadRepository<TAggregate, TId> {
    suspend fun siguienteId(): TId
    suspend fun guardar(agregado: TAggregate): Result<Unit>
    suspend fun actualizar(agregado: TAggregate): Result<Unit>
    suspend fun eliminar(id: TId): Result<Unit>
}
