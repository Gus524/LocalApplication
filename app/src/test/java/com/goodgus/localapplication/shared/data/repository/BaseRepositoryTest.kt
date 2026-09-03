package com.goodgus.localapplication.shared.data.repository

import com.goodgus.localapplication.DAO.BaseDao
import com.goodgus.localapplication.core.data.mapper.IMapper
import com.goodgus.localapplication.core.data.repository.BaseRepository
import com.goodgus.localapplication.core.domain.AggregateRoot
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class BaseRepositoryTest {

    private data class DummyId(val valor: String)

    private data class DummyAggregate(
        override val id: DummyId,
        val nombre: String
    ) : AggregateRoot<DummyId>

    private data class DummyPersistence(
        val id: String,
        val nombre: String
    )

    private class FakeBaseDao : BaseDao<DummyPersistence> {
        val databaseTable = mutableMapOf<String, DummyPersistence>()
        var throwOnNextInsert: Boolean = false

        override fun insert(entity: DummyPersistence): Long {
            if (throwOnNextInsert) throw IOException("Error de conexión a la base de datos")
            databaseTable[entity.id] = entity
            return 1L
        }

        override fun update(entity: DummyPersistence): Int {
            if (!databaseTable.containsKey(entity.id)) return 0
            databaseTable[entity.id] = entity
            return 1
        }

        override fun delete(entity: DummyPersistence): Int {
            return if (databaseTable.remove(entity.id) != null) 1 else 0
        }

        fun findById(id: String): DummyPersistence? = databaseTable[id]
    }

    private class DummyMapper : IMapper<DummyAggregate, DummyPersistence> {
        override fun toDomain(entity: DummyPersistence): DummyAggregate {
            return DummyAggregate(id = DummyId(entity.id), nombre = entity.nombre)
        }

        override fun toPersistence(domain: DummyAggregate): DummyPersistence {
            return DummyPersistence(id = domain.id.valor, nombre = domain.nombre)
        }
    }

    private class ConcreteDummyRepository(
        dao: FakeBaseDao,
        mapper: IMapper<DummyAggregate, DummyPersistence>,
        dispatcher: CoroutineDispatcher = Dispatchers.Unconfined
    ) : BaseRepository<DummyAggregate, DummyId, DummyPersistence, FakeBaseDao>(dao, mapper, dispatcher) {

        private var idCounter = 1

        override suspend fun onHydrateQuery(id: DummyId): DummyPersistence? {
            return dao.findById(id.valor)
        }

        override suspend fun onGetAllQuery(): List<DummyPersistence> {
            return dao.databaseTable.values.toList()
        }

        override suspend fun siguienteId(): DummyId = DummyId("dummy-${idCounter++}")

        fun mapearListaDominio(entities: List<DummyPersistence>): List<DummyAggregate> {
            return toDomainList(entities)
        }

        fun mapearListaPersistencia(domains: List<DummyAggregate>): List<DummyPersistence> {
            return toPersistenceList(domains)
        }
    }

    private lateinit var dao: FakeBaseDao
    private lateinit var mapper: DummyMapper
    private lateinit var repository: ConcreteDummyRepository

    @Before
    fun setup() {
        dao = FakeBaseDao()
        mapper = DummyMapper()
        repository = ConcreteDummyRepository(dao, mapper, Dispatchers.Unconfined)
    }

    @Test
    fun `IMapper realiza mapeo bidireccional puro 1 a 1`() {
        val domain = DummyAggregate(id = DummyId("101"), nombre = "Test Domain")
        val persistence = mapper.toPersistence(domain)

        assertEquals("101", persistence.id)
        assertEquals("Test Domain", persistence.nombre)

        val reconstructed = mapper.toDomain(persistence)
        assertEquals(domain, reconstructed)
    }

    @Test
    fun `BaseRepository toDomainList y toPersistenceList mapean listas usando el IMapper inyectado`() {
        val domainList = listOf(
            DummyAggregate(id = DummyId("1"), nombre = "A"),
            DummyAggregate(id = DummyId("2"), nombre = "B")
        )
        val persistenceList = repository.mapearListaPersistencia(domainList)

        assertEquals(2, persistenceList.size)
        assertEquals("A", persistenceList[0].nombre)
        assertEquals("B", persistenceList[1].nombre)

        val reconstructedList = repository.mapearListaDominio(persistenceList)
        assertEquals(domainList, reconstructedList)
    }

    @Test
    fun `BaseRepository guardar inserta en DAO y obtenerPorId hidrata y mapea al agregado`() = runBlocking {
        val id = repository.siguienteId()
        val aggregate = DummyAggregate(id = id, nombre = "Item Guardado")

        val resultadoGuardar = repository.guardar(aggregate)
        assertTrue(resultadoGuardar.isSuccess)

        val agregadoObtenido = repository.obtenerPorId(id)
        assertEquals(aggregate, agregadoObtenido)
        assertEquals("Item Guardado", dao.findById(id.valor)?.nombre)
    }

    @Test
    fun `BaseRepository actualizar actualiza correctamente el registro en el DAO`() = runBlocking {
        val id = repository.siguienteId()
        val aggregateInicial = DummyAggregate(id = id, nombre = "Nombre Original")
        repository.guardar(aggregateInicial)

        val aggregateModificado = DummyAggregate(id = id, nombre = "Nombre Actualizado")
        val resultadoActualizar = repository.actualizar(aggregateModificado)

        assertTrue(resultadoActualizar.isSuccess)
        assertEquals("Nombre Actualizado", dao.findById(id.valor)?.nombre)
        assertEquals("Nombre Actualizado", repository.obtenerPorId(id)?.nombre)
    }

    @Test
    fun `BaseRepository actualizar falla con Result failure si la entidad no existe en el DAO`() = runBlocking {
        val aggregateInexistente = DummyAggregate(id = DummyId("no-existe"), nombre = "Fantasma")
        val resultadoActualizar = repository.actualizar(aggregateInexistente)

        assertTrue(resultadoActualizar.isFailure)
        assertTrue(resultadoActualizar.exceptionOrNull() is NoSuchElementException)
    }

    @Test
    fun `BaseRepository eliminar remueve la entidad existente del DAO`() = runBlocking {
        val id = repository.siguienteId()
        val aggregate = DummyAggregate(id = id, nombre = "Por Eliminar")
        repository.guardar(aggregate)

        val resultadoEliminar = repository.eliminar(id)
        assertTrue(resultadoEliminar.isSuccess)
        assertNull(repository.obtenerPorId(id))
        assertNull(dao.findById(id.valor))
    }

    @Test
    fun `BaseRepository executeIo captura excepciones de IO de forma segura en Result failure`() = runBlocking {
        dao.throwOnNextInsert = true
        val aggregate = DummyAggregate(id = DummyId("error-1"), nombre = "Falla")

        val resultado = repository.guardar(aggregate)

        assertTrue(resultado.isFailure)
        val exception = resultado.exceptionOrNull()
        assertTrue(exception is IOException)
        assertEquals("Error de conexión a la base de datos", exception?.message)
    }

    @Test
    fun `BaseRepository obtenerTodos recupera y mapea todas las entidades`() = runBlocking {
        val item1 = DummyAggregate(id = DummyId("1"), nombre = "Item 1")
        val item2 = DummyAggregate(id = DummyId("2"), nombre = "Item 2")
        repository.guardar(item1)
        repository.guardar(item2)

        val todos = repository.obtenerTodos()
        assertEquals(2, todos.size)
        assertTrue(todos.any { it.id == DummyId("1") && it.nombre == "Item 1" })
        assertTrue(todos.any { it.id == DummyId("2") && it.nombre == "Item 2" })
    }
}
