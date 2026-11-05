package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.R
import com.example.ui.ActiveScreen
import com.example.ui.CalculatorViewModel
import com.example.ui.theme.*

@Composable
fun MainTopBar(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val isDark = themeMode == AppThemeMode.AMOLED_DARK

    GlassTopBar(viewModel = viewModel, isDark = isDark, modifier = modifier)
}

/** Menu with the two appearance modes (light / AMOLED dark). */
@Composable
private fun ThemeDropdown(
    expanded: Boolean,
    onDismiss: () -> Unit,
    viewModel: CalculatorViewModel,
    themeMode: AppThemeMode,
    isDark: Boolean
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
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
                onDismiss()
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
                onDismiss()
            }
        )
    }
}

// Both top bar images were cropped to the same size, with the buttons at the same height
private const val TOP_BAR_IMAGE_WIDTH = 2172f
private const val TOP_BAR_IMAGE_HEIGHT = 260f

/**
 * A top bar picture and the horizontal span (in image pixels) of its four buttons:
 * history, scope (info), theme (palette) and menu (hamburger).
 */
private class TopBarImage(
    val resId: Int,
    val history: ClosedFloatingPointRange<Float>,
    val scope: ClosedFloatingPointRange<Float>,
    val theme: ClosedFloatingPointRange<Float>,
    val menu: ClosedFloatingPointRange<Float>
)

private val lightTopBarImage = TopBarImage(
    resId = R.drawable.top_bar_glass,
    history = 63f..286f,
    scope = 308f..530f,
    theme = 544f..765f,
    menu = 1936f..2146f
)

private val darkTopBarImage = TopBarImage(
    resId = R.drawable.top_bar_glass_dark,
    history = 62f..286f,
    scope = 310f..525f,
    theme = 547f..761f,
    menu = 1967f..2168f
)

/**
 * The top bar: the supplied image for the current theme (shown exactly as it is) with an
 * invisible tap area over each of its four buttons.
 */
@Composable
private fun GlassTopBar(
    viewModel: CalculatorViewModel,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val themeMode by viewModel.themeMode.collectAsState()
    var themeMenuOpen by remember { mutableStateOf(false) }
    val bar = if (isDark) darkTopBarImage else lightTopBarImage

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .aspectRatio(TOP_BAR_IMAGE_WIDTH / TOP_BAR_IMAGE_HEIGHT)
    ) {
        Image(
            painter = painterResource(id = bar.resId),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )

        @Composable
        fun TapArea(
            span: ClosedFloatingPointRange<Float>,
            tag: String,
            description: String,
            onClick: () -> Unit,
            content: @Composable BoxScope.() -> Unit = {}
        ) {
            Box(
                modifier = Modifier
                    .offset(x = maxWidth * (span.start / TOP_BAR_IMAGE_WIDTH))
                    .width(maxWidth * ((span.endInclusive - span.start) / TOP_BAR_IMAGE_WIDTH))
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(18.dp))
                    .clickable(role = Role.Button, onClick = onClick)
                    .semantics { contentDescription = description }
                    .testTag(tag),
                content = content
            )
        }

        TapArea(
            bar.history, "top_bar_history_button", "تاریخچه",
            onClick = { viewModel.setHistoryOpen(true) }
        )
        TapArea(
            bar.scope, "top_bar_scope_button", "دامنه و راهنما",
            onClick = { viewModel.setScopeOpen(true) }
        )
        TapArea(
            bar.theme, "top_bar_theme_button", "ظاهر",
            onClick = { themeMenuOpen = true }
        ) {
            ThemeDropdown(
                expanded = themeMenuOpen,
                onDismiss = { themeMenuOpen = false },
                viewModel = viewModel,
                themeMode = themeMode,
                isDark = isDark
            )
        }
        TapArea(
            bar.menu, "top_bar_hamburger_button", "منو",
            onClick = { viewModel.setDrawerOpen(true) }
        )
    }
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
