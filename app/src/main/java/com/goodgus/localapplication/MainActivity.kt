package com.goodgus.localapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.goodgus.localapplication.DAO.AppDataBase
import com.goodgus.localapplication.DAO.ProductoDAO
import com.goodgus.localapplication.application.LocalApplication
import com.goodgus.localapplication.core.navigation.NavigationWrapper
import com.goodgus.localapplication.repository.ProductoRepository
import com.goodgus.localapplication.ui.theme.LocalApplicationTheme
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.flow.toList

class MainActivity : ComponentActivity() {
    // Se declaran las propiedades pero no se inicializan
    private lateinit var app: LocalApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = applicationContext as LocalApplication
        enableEdgeToEdge()
        setContent {
            LocalApplicationTheme {
                NavigationWrapper()
            }
        }
    }
}
