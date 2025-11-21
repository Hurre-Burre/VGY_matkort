package com.example.vgy_matkort

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vgy_matkort.ui.AppNavigation
import com.example.vgy_matkort.ui.HomeScreen
import com.example.vgy_matkort.ui.MainViewModel
import com.example.vgy_matkort.ui.theme.VGY_MatkortTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val viewModel: MainViewModel = viewModel()
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()
            
            VGY_MatkortTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        viewModel = viewModel,
                        isDarkTheme = isDarkTheme,
                        onToggleTheme = viewModel::toggleTheme
                    )
                }
            }
        }
    }
}