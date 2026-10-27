package com.example.furnituregap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.furnituregap.data.model.AppThemeMode
import com.example.furnituregap.ui.navigation.AppNavHost
import com.example.furnituregap.ui.theme.FurnitureGapTheme
import com.example.furnituregap.ui.viewmodel.FurnitureGapViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: FurnitureGapViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by viewModel.settings.collectAsState()
            val systemDark = isSystemInDarkTheme()

            val isDarkTheme = when (settings.themeMode) {
                AppThemeMode.SYSTEM -> systemDark
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
            }

            FurnitureGapTheme(
                darkTheme = isDarkTheme,
                dynamicColor = settings.dynamicColor
            ) {
                AppNavHost(viewModel = viewModel)
            }
        }
    }
}
