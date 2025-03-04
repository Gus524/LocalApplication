package com.goodgus.localapplication.DAO

import androidx.room.Dao
import androidx.room.Query
import com.goodgus.localapplication.models.data.Compra

/**
 * Interfaz de los querys para nuestra tabla Compra
 */

@Dao
interface CompraDAO {
    @Query("SELECT * FROM Compra")
    fun getCompra(): List<Compra>

}