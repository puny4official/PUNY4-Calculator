package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppMode
import com.example.ui.CalculatorViewModel
import com.example.ui.theme.AmoledBackground
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.LightBackground
import com.example.ui.theme.LightTextPrimary
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.PurpleSecondary
import com.example.ui.theme.TextPrimary

@Composable
fun MainTopBar(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val currentMode by viewModel.currentMode.collectAsState()
    val themeMode by viewModel.appThemeMode.collectAsState()
    val isDark = themeMode == AppThemeMode.AMOLED_DARK

    val barBg = if (isDark) AmoledBackground else LightBackground
    val titleTextColor = if (isDark) TextPrimary else LightTextPrimary

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .background(barBg)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .testTag("main_top_app_bar"),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Title + Subtitle
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "PUNY4 Math Studio",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                    color = titleTextColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (currentMode == AppMode.NORMAL) "ماشین‌حساب عادی" else "استودیو محاسبات ریاضی",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PurpleSecondary
                )
            }

            // Right Hamburger Menu ≡ (opens half-screen drawer)
            Box {
                IconButton(
                    onClick = {
                        viewModel.isDrawerOpen.value = true
                    },
                    modifier = Modifier.testTag("hamburger_menu_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "منو",
                        tint = PurplePrimary,
                        modifier = Modifier.padding(2.dp)
                    )
                }
            }
        }
    }
}
