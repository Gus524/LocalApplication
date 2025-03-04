package com.goodgus.localapplication.application

import android.app.Application
import com.goodgus.localapplication.DAO.AppDataBase

/*
 * Funcion que inicializa la base de datos con patron singleton
 *
 */

class LocalApplication: Application() {
    val database: AppDataBase by lazy {
        AppDataBase.getInstance(this)
    }
}