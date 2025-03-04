package com.goodgus.localapplication.utilidades

import androidx.room.TypeConverter
import java.util.Date

/*
 * Clase converter, se utiliza para Room (creo que no lo termine aplicando en este proyecto)
 *
 */

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}