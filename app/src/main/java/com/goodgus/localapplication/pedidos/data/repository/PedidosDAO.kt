package com.goodgus.localapplication.pedidos.data.repository

import androidx.room.Dao
import androidx.room.Query
import com.goodgus.localapplication.core.data.dao.BaseDao
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz de los querys para nuestra tabla Pedidos
 */
@Dao
interface PedidosDAO : BaseDao<Pedidos> {

    @Query("SELECT * FROM Pedidos WHERE id_pedido = :id")
    fun getById(id: Int): Pedidos?

    @Query("SELECT * FROM Pedidos")
    fun getAll(): List<Pedidos>

    @Query("SELECT MAX(id_pedido) FROM Pedidos")
    fun getMaxId(): Int?

    @Query("SELECT * FROM Pedidos")
    fun getPedidos(): Flow<List<Pedidos>>
}