package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AppMode
import com.example.ui.CalculatorViewModel
import com.example.ui.components.AppDrawer
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: CalculatorViewModel = viewModel()
            val themeMode by viewModel.appThemeMode.collectAsState()

            Puny4Theme(themeMode = themeMode) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Puny4App(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun Puny4App(
    viewModel: CalculatorViewModel
) {
    val currentMode by viewModel.currentMode.collectAsState()
    val showHistory by viewModel.showHistoryDialog.collectAsState()
    val showScope by viewModel.showScopeDialog.collectAsState()
    val isDrawerOpen by viewModel.isDrawerOpen.collectAsState()
    val themeMode by viewModel.appThemeMode.collectAsState()
    val isDark = themeMode == AppThemeMode.AMOLED_DARK

    val appBg = if (isDark) AmoledBackground else LightBackground

    // Back handler: close drawer first, or switch from Math Studio to Normal before exiting
    BackHandler(enabled = isDrawerOpen || currentMode == AppMode.MATH_STUDIO) {
        if (isDrawerOpen) {
            viewModel.isDrawerOpen.value = false
        } else if (currentMode == AppMode.MATH_STUDIO) {
            viewModel.setAppMode(AppMode.NORMAL)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(appBg),
            topBar = {
                MainTopBar(viewModel = viewModel)
            },
            containerColor = appBg
        ) { innerPadding ->
            AnimatedContent(
                targetState = currentMode,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "mode_transition",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) { mode ->
                when (mode) {
                    AppMode.NORMAL -> NormalCalculatorScreen(viewModel = viewModel)
                    AppMode.MATH_STUDIO -> MathStudioScreen(viewModel = viewModel)
                }
            }

            if (showHistory) {
                HistoryDialog(
                    viewModel = viewModel,
                    onDismiss = { viewModel.showHistoryDialog.value = false }
                )
            }

            if (showScope) {
                ScopeDialog(
                    onDismiss = { viewModel.showScopeDialog.value = false }
                )
            }
        }

        // Half-Screen Slide Drawer
        AppDrawer(
            viewModel = viewModel,
            isOpen = isDrawerOpen,
            onClose = { viewModel.isDrawerOpen.value = false }
        )
    }
}
