package com.goodgus.localapplication.ventas.data.repository

import com.goodgus.localapplication.inventario.data.repository.ProductoDAO
import com.goodgus.localapplication.core.data.repository.BaseRepository
import com.goodgus.localapplication.ventas.data.repository.CuentaEntity
import com.goodgus.localapplication.ventas.data.mapper.CuentaMapper
import com.goodgus.localapplication.ventas.domain.model.Cuenta
import com.goodgus.localapplication.ventas.domain.model.CuentaId
import com.goodgus.localapplication.ventas.domain.repository.ICuentaRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CuentaRepository @Inject constructor(
    dao: CuentaDAO,
    private val productoDao: ProductoDAO,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseRepository<Cuenta, CuentaId, CuentaEntity, CuentaDAO>(dao, CuentaMapper(), ioDispatcher),
    ICuentaRepository {

    private val cuentaMapper = CuentaMapper()

    override suspend fun onHydrateQuery(id: CuentaId): CuentaEntity? {
        return dao.getById(id.valor)
    }

    override suspend fun onGetAllQuery(): List<CuentaEntity> {
        return dao.getAll()
    }

    override suspend fun siguienteId(): CuentaId = executeIo {
        CuentaId((dao.getMaxId() ?: 0) + 1)
    }.getOrDefault(CuentaId(1))

    override suspend fun onHydrateAggregate(id: CuentaId): Cuenta? {
        val entity = onHydrateQuery(id) ?: return null
        val ventas = dao.getVentasByCuentaId(id.valor)
        val nombresMap = ventas.associate { v ->
            val nombre = productoDao.getById(v.idProducto)?.nombre ?: "Producto #${v.idProducto}"
            v.idProducto to nombre
        }
        return cuentaMapper.toDomain(entity, ventas, nombresMap)
    }

    override suspend fun obtenerCuentaActiva(): Cuenta? = executeIo {
        val activas = dao.getCuentaActivaNoVentas()
        val activaEntity = activas.firstOrNull() ?: return@executeIo null
        onHydrateAggregate(CuentaId(activaEntity.idCuenta))
    }.getOrNull()

    override fun observarCuentaActiva(): Flow<Cuenta?> {
        return dao.getCuentaActiva()
            .map { list ->
                val primerItem = list.firstOrNull()
                if (primerItem != null) {
                    onHydrateAggregate(CuentaId(primerItem.idCuenta))
                } else {
                    val activas = dao.getCuentaActivaNoVentas()
                    val activaEntity = activas.firstOrNull()
                    if (activaEntity != null) {
                        onHydrateAggregate(CuentaId(activaEntity.idCuenta))
                    } else {
                        null
                    }
                }
            }
            .flowOn(ioDispatcher)
    }

    override suspend fun guardar(agregado: Cuenta): Result<Unit> = executeIo {
        val entity = cuentaMapper.toPersistence(agregado)
        dao.insert(entity)
        if (agregado.ventas.isNotEmpty()) {
            val ventasEntities = agregado.ventas.map {
                cuentaMapper.toVentaPersistence(it, agregado.id)
            }
            dao.insertVentas(ventasEntities)
        }
    }

    override suspend fun actualizar(agregado: Cuenta): Result<Unit> = executeIo {
        val entity = cuentaMapper.toPersistence(agregado)
        val updated = dao.update(entity)
        if (updated == 0) {
            throw NoSuchElementException("No se encontró la cuenta para actualizar con ID: ${agregado.id}")
        }
        dao.deleteVentasByCuentaId(agregado.id.valor)
        if (agregado.ventas.isNotEmpty()) {
            val ventasEntities = agregado.ventas.map {
                cuentaMapper.toVentaPersistence(it, agregado.id)
            }
            dao.insertVentas(ventasEntities)
        }
    }
}
