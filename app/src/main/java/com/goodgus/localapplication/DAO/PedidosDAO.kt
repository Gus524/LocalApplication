package com.goodgus.localapplication.DAO

import androidx.room.Dao
import androidx.room.Query
import com.goodgus.localapplication.models.data.Pedidos
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz de los querys para nuestra tabla Pedidos
 *
 * Se establecen los querys para obtener los pedidos y agregar un nuevo pedido
 * TODO agregar los querys para actualizar los pedidos
 */


@Dao
interface PedidosDAO {
    @Query("SELECT * FROM Pedidos")
    fun getPedidos(): Flow<List<Pedidos>>

    @Query("INSERT INTO Pedidos (fecha_pedido, descripcion, detalles) VALUES (:fechaPedido, :descripcion, :detalles)")
    fun addPedido(fechaPedido: String, descripcion: String, detalles: String): Long

}