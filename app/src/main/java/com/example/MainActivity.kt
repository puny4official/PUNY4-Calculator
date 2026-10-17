package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.ui.ActiveScreen
import com.example.ui.CalculatorViewModel
import com.example.ui.components.HamburgerSideSheet
import com.example.ui.components.HistoryDialog
import com.example.ui.components.MainTopBar
import com.example.ui.components.ScopeDialog
import com.example.ui.screens.MathStudioScreen
import com.example.ui.screens.NormalCalculatorScreen
import com.example.ui.theme.AmoledBackground
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.LightBackground
import com.example.ui.theme.Puny4Theme

class MainActivity : ComponentActivity() {

    private val viewModel: CalculatorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val currentScreen by viewModel.currentScreen.collectAsState()
            val isDrawerOpen by viewModel.isDrawerOpen.collectAsState()
            val isHistoryOpen by viewModel.isHistoryOpen.collectAsState()
            val isScopeOpen by viewModel.isScopeOpen.collectAsState()

            // Handle back presses smoothly
            BackHandler(enabled = isDrawerOpen) {
                viewModel.setDrawerOpen(false)
            }
            BackHandler(enabled = !isDrawerOpen && currentScreen == ActiveScreen.STUDIO) {
                viewModel.setScreen(ActiveScreen.NORMAL)
            }

            Puny4Theme(themeMode = themeMode) {
                val isDark = themeMode == AppThemeMode.AMOLED_DARK
                val mainBg = if (isDark) AmoledBackground else LightBackground

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(mainBg)
                ) {
                    Scaffold(
                        topBar = {
                            MainTopBar(viewModel = viewModel)
                        },
                        containerColor = mainBg,
                        contentWindowInsets = WindowInsets.safeDrawing
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when (currentScreen) {
                                ActiveScreen.NORMAL -> NormalCalculatorScreen(viewModel = viewModel)
                                ActiveScreen.STUDIO -> MathStudioScreen(viewModel = viewModel)
                            }
                        }
                    }

                    // Hamburger Side Sheet (opens halfway across screen from top to bottom)
                    HamburgerSideSheet(viewModel = viewModel)

                    // History Dialog
                    if (isHistoryOpen) {
                        HistoryDialog(
                            viewModel = viewModel,
                            onDismiss = { viewModel.setHistoryOpen(false) }
                        )
                    }

                    // Scope Dialog
                    if (isScopeOpen) {
                        ScopeDialog(
                            viewModel = viewModel,
                            onDismiss = { viewModel.setScopeOpen(false) }
                        )
                    }
                }
            }
        }
    }
}
