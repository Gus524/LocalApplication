package com.goodgus.localapplication.ventas.data.repository

import com.goodgus.localapplication.DAO.CuentaDAO
import com.goodgus.localapplication.DAO.ProductoDAO
import com.goodgus.localapplication.core.data.repository.BaseRepository
import com.goodgus.localapplication.models.data.Cuenta as CuentaEntity
import com.goodgus.localapplication.ventas.data.mapper.CuentaMapper
import com.goodgus.localapplication.ventas.domain.model.Cuenta
import com.goodgus.localapplication.ventas.domain.model.CuentaId
import com.goodgus.localapplication.ventas.domain.repository.ICuentaRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class CuentaRepository @Inject constructor(
    dao: CuentaDAO,
    private val productoDao: ProductoDAO,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseRepository<Cuenta, CuentaId, CuentaEntity, CuentaDAO>(dao, CuentaMapper(), ioDispatcher),
    ICuentaRepository {

    private val cuentaMapper = CuentaMapper()

    override suspend fun onHydrateQuery(id: CuentaId): CuentaEntity? {
        return dao.getCuentaById(id.valor)
    }

    override suspend fun onGetAllQuery(): List<CuentaEntity> {
        return dao.getAllCuentas()
    }

    override suspend fun siguienteId(): CuentaId = executeIo {
        CuentaId((dao.getMaxId() ?: 0) + 1)
    }.getOrDefault(CuentaId(1))

    override suspend fun obtenerPorId(id: CuentaId): Cuenta? = executeIo {
        val entity = onHydrateQuery(id) ?: return@executeIo null
        val ventas = dao.getVentasByCuentaId(id.valor)
        val nombresMap = ventas.associate { v ->
            val nombre = productoDao?.getProductId(v.idProducto)?.nombre ?: "Producto #${v.idProducto}"
            v.idProducto to nombre
        }
        cuentaMapper.toDomain(entity, ventas, nombresMap)
    }.getOrNull()

    override suspend fun obtenerCuentaActiva(): Cuenta? = executeIo {
        val activas = dao.getCuentaActivaNoVentas()
        val activaEntity = activas.firstOrNull() ?: return@executeIo null
        obtenerPorId(CuentaId(activaEntity.idCuenta))
    }.getOrNull()

    override suspend fun guardar(agregado: Cuenta): Result<Unit> = executeIo {
        val entity = cuentaMapper.toPersistence(agregado)
        dao.insert(entity)
        if (agregado.ventas.isNotEmpty()) {
            val ventasEntities = agregado.ventas.map {
                cuentaMapper.toVentaPersistence(it, agregado.id)
            }
            dao.insertVentas(ventasEntities)
        }
        Unit
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
        Unit
    }
}
