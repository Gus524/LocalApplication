package com.goodgus.localapplication.core.data.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update

/**
 * Contrato base genérico de Room para operaciones CRUD elementales.
 * Actúa como la abstracción análoga a DbSet<T> en la infraestructura de persistencia.
 */
interface BaseDao<T> {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entity: T): Long

    @Update
    fun update(entity: T): Int

    @Delete
    fun delete(entity: T): Int
}
