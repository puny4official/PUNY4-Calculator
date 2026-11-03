package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.ui.ActiveScreen
import com.example.ui.CalculatorViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopBar(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val isDark = themeMode == AppThemeMode.AMOLED_DARK
    val historyButtonShape = RoundedCornerShape(16.dp)
    val historyGlass = Brush.linearGradient(
        colors = listOf(
            Color(0xFFFFF4F8).copy(alpha = 0.92f),
            Color(0xFFFFC4DC).copy(alpha = 0.72f),
            Color(0xFFF472B6).copy(alpha = 0.34f)
        )
    )
    val historyGlassBorder = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.96f),
            Color(0xFFE879A9).copy(alpha = 0.48f)
        )
    )

    TopAppBar(
        title = {},
        navigationIcon = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // History Button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(historyButtonShape)
                        .background(historyGlass)
                        .border(1.dp, historyGlassBorder, historyButtonShape)
                        .clickable(role = Role.Button) { viewModel.setHistoryOpen(true) }
                        .testTag("top_bar_history_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "تاریخچه",
                        tint = Color(0xFFC2185B),
                        modifier = Modifier.size(26.dp)
                    )
                }

                // Scope Button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(historyButtonShape)
                        .background(historyGlass)
                        .border(1.dp, historyGlassBorder, historyButtonShape)
                        .clickable(role = Role.Button) { viewModel.setScopeOpen(true) }
                        .testTag("top_bar_scope_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "دامنه و راهنما",
                        tint = Color(0xFFC2185B),
                        modifier = Modifier.size(26.dp)
                    )
                }

                // Theme (Appearance) Button
                var themeMenuOpen by remember { mutableStateOf(false) }
                Box {
                    IconButton(
                        onClick = { themeMenuOpen = true },
                        modifier = Modifier.testTag("top_bar_theme_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "ظاهر",
                            tint = PurpleSecondary
                        )
                    }
                    DropdownMenu(
                        expanded = themeMenuOpen,
                        onDismissRequest = { themeMenuOpen = false },
                        modifier = Modifier.background(if (isDark) AmoledDisplaySurface else LightDisplaySurface)
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "لایت مود",
                                    color = if (isDark) TextPrimary else LightTextPrimary
                                )
                            },
                            leadingIcon = {
                                Icon(Icons.Default.LightMode, contentDescription = null, tint = PurpleSecondary)
                            },
                            trailingIcon = {
                                if (themeMode == AppThemeMode.LIGHT) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = PurplePrimary)
                                }
                            },
                            onClick = {
                                viewModel.setThemeMode(AppThemeMode.LIGHT)
                                themeMenuOpen = false
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "دارک مود امولد",
                                    color = if (isDark) TextPrimary else LightTextPrimary
                                )
                            },
                            leadingIcon = {
                                Icon(Icons.Default.DarkMode, contentDescription = null, tint = PurpleSecondary)
                            },
                            trailingIcon = {
                                if (themeMode == AppThemeMode.AMOLED_DARK) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = PurplePrimary)
                                }
                            },
                            onClick = {
                                viewModel.setThemeMode(AppThemeMode.AMOLED_DARK)
                                themeMenuOpen = false
                            }
                        )
                    }
                }
            }
        },
        actions = {
            // App name next to the hamburger menu
            Text(
                text = "PUNY4 Calculator",
                color = if (isDark) TextPrimary else LightTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            // Hamburger Menu Button (Top Right)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(historyButtonShape)
                    .background(historyGlass)
                    .border(1.dp, historyGlassBorder, historyButtonShape)
                    .clickable(role = Role.Button) { viewModel.setDrawerOpen(true) }
                    .testTag("top_bar_hamburger_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "منو",
                    tint = Color(0xFFC2185B),
                    modifier = Modifier.size(26.dp)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = if (isDark) AmoledBackground else LightBackground
        ),
        modifier = modifier
    )
}

/**
 * Hamburger Side Sheet that opens halfway across the screen (top to bottom)
 * as explicitly requested: "یه صفحه کوچیک تا نصف صفحه از بالا تا پایین باز شه"
 * With app name "PUNY4 Calculator" on top, modes, and appearance toggle (AMOLED Dark / Light)
 */
@Composable
fun HamburgerSideSheet(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val isOpen by viewModel.isDrawerOpen.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val isDark = themeMode == AppThemeMode.AMOLED_DARK

    if (isOpen) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    viewModel.setDrawerOpen(false)
                }
        ) {
            // Half-screen slide panel aligned to the right (or end)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.58f) // Half screen from top to bottom!
                    .align(Alignment.CenterEnd)
                    .background(if (isDark) AmoledDisplaySurface else LightDisplaySurface)
                    .border(
                        1.dp,
                        if (isDark) BlackBorder else LightBorder,
                        RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        // Prevent dismiss when tapping inside sheet
                    }
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .padding(horizontal = 16.dp, vertical = 20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // TOP SECTION: APP NAME & NAVIGATION
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Top Header: App Name ("در بالا اسم اپ خودم رو بزن")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "PUNY4",
                                    color = PurplePrimary,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "Calculator",
                                    color = if (isDark) TextSecondary else LightTextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            IconButton(
                                onClick = { viewModel.setDrawerOpen(false) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "بستن",
                                    tint = if (isDark) TextSecondary else LightTextSecondary
                                )
                            }
                        }

                        Divider(color = if (isDark) BlackBorder else LightBorder)

                        // 1. ماشین‌حساب عادی
                        DrawerMenuItem(
                            title = "ماشین‌حساب عادی",
                            icon = Icons.Default.Calculate,
                            isSelected = currentScreen == ActiveScreen.NORMAL,
                            isDark = isDark,
                            onClick = {
                                viewModel.setScreen(ActiveScreen.NORMAL)
                            }
                        )

                        // 2. ماشین‌حساب Math Studio
                        DrawerMenuItem(
                            title = "ماشین‌حساب Math Studio",
                            icon = Icons.Default.AutoGraph,
                            isSelected = currentScreen == ActiveScreen.STUDIO,
                            isDark = isDark,
                            onClick = {
                                viewModel.setScreen(ActiveScreen.STUDIO)
                            }
                        )
                    }

                    // BOTTOM SECTION: App version
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Divider(color = if (isDark) BlackBorder else LightBorder)

                        Text(
                            text = "نسخه ${BuildConfig.VERSION_NAME}",
                            color = if (isDark) TextSecondary else LightTextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DrawerMenuItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val bg = if (isSelected) PurplePrimary else Color.Transparent
    val contentCol = if (isSelected) Color.White else (if (isDark) TextPrimary else LightTextPrimary)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) Color.White else PurpleSecondary,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = title,
            color = contentCol,
            fontSize = 13.5.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
