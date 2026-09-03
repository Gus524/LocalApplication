package com.goodgus.localapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.goodgus.localapplication.core.navigation.NavigationWrapper
import com.goodgus.localapplication.core.theme.LocalApplicationTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LocalApplicationTheme {
                NavigationWrapper()
            }
        }
    }
}
