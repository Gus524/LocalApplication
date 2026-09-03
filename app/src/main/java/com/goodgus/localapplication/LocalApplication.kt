package com.goodgus.localapplication

import android.app.Application
import com.goodgus.localapplication.DAO.AppDataBase
import dagger.hilt.android.HiltAndroidApp

/*
 * Inicialización de la aplicación con Hilt
 */
@HiltAndroidApp
class LocalApplication : Application() {
    val database: AppDataBase by lazy {
        AppDataBase.getInstance(this)
    }
}