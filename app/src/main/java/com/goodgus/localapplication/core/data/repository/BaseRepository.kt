package com.goodgus.localapplication.core.data.repository

import com.goodgus.localapplication.core.data.dao.BaseDao
import com.goodgus.localapplication.core.data.mapper.IMapper
import com.goodgus.localapplication.core.domain.AggregateRoot
import com.goodgus.localapplication.core.domain.IRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Adaptador de Infraestructura Base inspirado en el patrón GenericRepository con Template Method.
 * Encapsula la hidratación, operaciones CRUD base, mapeo y despacho de corrutinas con tipado estricto.
 *
 * @param TAggregate Raíz de Agregado del dominio puro.
 * @param TId Identificador fuertemente tipado (Value Object) del Agregado.
 * @param TPersistence Modelo de entidad/datos acoplado a Room.
 * @param TDao Interfaz DAO de Room que implementa operaciones CRUD base (BaseDao).
 */
abstract class BaseRepository<TAggregate : AggregateRoot<TId>, TId, TPersistence, TDao : BaseDao<TPersistence>>(
    protected val dao: TDao,
    protected val mapper: IMapper<TAggregate, TPersistence>,
    protected val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : IRepository<TAggregate, TId> {

    /**
     * Hook Template Method (Hydration Query): La implementación concreta ejecuta la consulta
     * de la entidad de persistencia por su ID.
     */
    protected abstract suspend fun onHydrateQuery(id: TId): TPersistence?

    /**
     * Hook Template Method (List Query): La implementación concreta ejecuta la consulta
     * de listado general de entidades de persistencia.
     */
    protected abstract suspend fun onGetAllQuery(): List<TPersistence>

    /**
     * Hook Template Method (Hydration Aggregate): Hidrata el agregado de dominio completo por ID.
     * Por defecto consulta la entidad de persistencia vía onHydrateQuery(id) y la mapea a dominio.
     * Los agregados complejos (con entidades hijas) sobreescriben este método para resolver el árbol.
     */
    protected open suspend fun onHydrateAggregate(id: TId): TAggregate? {
        val persistence = onHydrateQuery(id) ?: return null
        return mapper.toDomain(persistence)
    }

    override suspend fun obtenerPorId(id: TId): TAggregate? = executeIo {
        onHydrateAggregate(id)
    }.getOrNull()

    override suspend fun obtenerTodos(): List<TAggregate> = executeIo {
        toDomainList(onGetAllQuery())
    }.getOrDefault(emptyList())

    override suspend fun guardar(agregado: TAggregate): Result<Unit> = executeIo {
        val persistence = mapper.toPersistence(agregado)
        dao.insert(persistence)
        Unit
    }

    override suspend fun actualizar(agregado: TAggregate): Result<Unit> = executeIo {
        val persistence = mapper.toPersistence(agregado)
        val updatedRows = dao.update(persistence)
        if (updatedRows == 0) {
            throw NoSuchElementException("No se encontró la entidad para actualizar con ID: ${agregado.id}")
        }
    }

    override suspend fun eliminar(id: TId): Result<Unit> = executeIo {
        val persistence = onHydrateQuery(id)
        if (persistence != null) {
            dao.delete(persistence)
        }
        Unit
    }

    // --- Helpers protegidos para listas y despacho ---
    protected fun toDomainList(entities: List<TPersistence>): List<TAggregate> =
        entities.map { mapper.toDomain(it) }

    protected fun toPersistenceList(domains: List<TAggregate>): List<TPersistence> =
        domains.map { mapper.toPersistence(it) }

    protected suspend fun <R> executeIo(block: suspend () -> R): Result<R> =
        withContext(ioDispatcher) {
            runCatching { block() }
        }
}
