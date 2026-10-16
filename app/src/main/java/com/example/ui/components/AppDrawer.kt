package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppMode
import com.example.ui.CalculatorViewModel
import com.example.ui.theme.AmoledBackground
import com.example.ui.theme.AmoledDisplaySurface
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.LightBackground
import com.example.ui.theme.LightBorder
import com.example.ui.theme.LightDisplaySurface
import com.example.ui.theme.LightKeyOperatorBg
import com.example.ui.theme.LightKeyOperatorText
import com.example.ui.theme.LightTextPrimary
import com.example.ui.theme.LightTextSecondary
import com.example.ui.theme.LightTextMuted
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.PurpleSecondary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AppDrawer(
    viewModel: CalculatorViewModel,
    isOpen: Boolean,
    onClose: () -> Unit
) {
    val themeMode by viewModel.appThemeMode.collectAsState()
    val isDark = themeMode == AppThemeMode.AMOLED_DARK
    val currentMode by viewModel.currentMode.collectAsState()

    val drawerBg = if (isDark) AmoledDisplaySurface else LightDisplaySurface
    val textPrimaryColor = if (isDark) TextPrimary else LightTextPrimary
    val textSecondaryColor = if (isDark) TextSecondary else LightTextSecondary
    val borderColor = if (isDark) Color(0xFF2A1C4E) else LightBorder

    // Full screen overlay when drawer is open
    AnimatedVisibility(
        visible = isOpen,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClose
                ),
            contentAlignment = Alignment.CenterEnd
        ) {
            // Half-screen drawer (covers ~65% of screen from top to bottom)
            AnimatedVisibility(
                visible = isOpen,
                enter = slideInHorizontally(initialOffsetX = { it }),
                exit = slideOutHorizontally(targetOffsetX = { it })
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.68f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {} // Consume click inside
                        )
                        .testTag("half_screen_side_drawer"),
                    color = drawerBg,
                    shape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 18.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            // Top Header with App Name
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "PUNY4 Math Studio",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.SansSerif,
                                        color = PurplePrimary
                                    )
                                    Text(
                                        text = "ماشین‌حساب هوشمند و استودیو ریاضی",
                                        fontSize = 10.sp,
                                        color = textSecondaryColor,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }

                                IconButton(onClick = onClose) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "بستن",
                                        tint = textSecondaryColor
                                    )
                                }
                            }

                            HorizontalDivider(
                                color = borderColor,
                                modifier = Modifier.padding(vertical = 14.dp)
                            )

                            // SECTION 1: CALCULATOR MODES
                            DrawerSectionTitle("حالت‌های محاسبه", textSecondaryColor)
                            Spacer(modifier = Modifier.height(6.dp))

                            DrawerModeItem(
                                title = "ماشین‌حساب عادی",
                                subtitle = "محاسبات سریع و علمی",
                                icon = Icons.Default.Calculate,
                                isSelected = currentMode == AppMode.NORMAL,
                                isDark = isDark,
                                onClick = {
                                    viewModel.setAppMode(AppMode.NORMAL)
                                    onClose()
                                }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            DrawerModeItem(
                                title = "استودیو Math Studio",
                                subtitle = "فرمول‌ها و حل گام‌به‌گام",
                                icon = Icons.Default.Functions,
                                isSelected = currentMode == AppMode.MATH_STUDIO,
                                isDark = isDark,
                                onClick = {
                                    viewModel.setAppMode(AppMode.MATH_STUDIO)
                                    onClose()
                                }
                            )

                            HorizontalDivider(
                                color = borderColor,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )

                            // SECTION 2: APPEARANCE (لایت مود / دارک مود امولد)
                            DrawerSectionTitle("ظاهر و پوسته", textSecondaryColor)
                            Spacer(modifier = Modifier.height(8.dp))

                            // Light Mode Option
                            DrawerThemeOption(
                                title = "حالت روشن (Light Mode)",
                                subtitle = "پیش‌فرض خودکار برنامه",
                                icon = Icons.Default.LightMode,
                                isSelected = themeMode == AppThemeMode.LIGHT,
                                isDark = isDark,
                                onClick = {
                                    viewModel.appThemeMode.value = AppThemeMode.LIGHT
                                }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // AMOLED Dark Mode Option
                            DrawerThemeOption(
                                title = "دارک مود امولد (AMOLED)",
                                subtitle = "مشکی عمیق با بنفش نئونی",
                                icon = Icons.Default.DarkMode,
                                isSelected = themeMode == AppThemeMode.AMOLED_DARK,
                                isDark = isDark,
                                onClick = {
                                    viewModel.appThemeMode.value = AppThemeMode.AMOLED_DARK
                                }
                            )

                            HorizontalDivider(
                                color = borderColor,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )

                            // SECTION 3: TOOLS & HISTORY
                            DrawerSectionTitle("ابزارها", textSecondaryColor)
                            Spacer(modifier = Modifier.height(6.dp))

                            DrawerToolItem(
                                title = "تاریخچه و نشان‌شده‌ها",
                                icon = Icons.Default.History,
                                isDark = isDark,
                                onClick = {
                                    viewModel.showHistoryDialog.value = true
                                    onClose()
                                }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            DrawerToolItem(
                                title = "شفاف‌سازی و دامنه ریاضی",
                                icon = Icons.Default.Info,
                                isDark = isDark,
                                onClick = {
                                    viewModel.showScopeDialog.value = true
                                    onClose()
                                }
                            )
                        }

                        // Drawer Footer
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "نسخه ۱.۰ • کاملاً آفلاین",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DrawerSectionTitle(title: String, color: Color) {
    Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = PurpleSecondary,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
    )
}

@Composable
fun DrawerModeItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val bg = if (isSelected) {
        if (isDark) Color(0xFF261947) else Color(0xFFEDE9FE)
    } else {
        if (isDark) Color(0xFF130E26) else Color(0xFFF8FAFC)
    }

    val border = if (isSelected) PurplePrimary else Color.Transparent

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        color = bg,
        border = androidx.compose.foundation.BorderStroke(1.dp, border),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) PurplePrimary else if (isDark) TextSecondary else LightTextSecondary,
                    modifier = Modifier.padding(end = 10.dp)
                )
                Column {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isDark) TextPrimary else LightTextPrimary
                    )
                    Text(
                        text = subtitle,
                        fontSize = 10.sp,
                        color = if (isDark) TextMuted else LightTextMuted
                    )
                }
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = PurplePrimary
                )
            }
        }
    }
}

@Composable
fun DrawerThemeOption(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val bg = if (isSelected) {
        if (isDark) Color(0xFF261947) else Color(0xFFEDE9FE)
    } else {
        if (isDark) Color(0xFF130E26) else Color(0xFFF8FAFC)
    }

    val border = if (isSelected) PurplePrimary else Color.Transparent

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        color = bg,
        border = androidx.compose.foundation.BorderStroke(1.dp, border),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) PurplePrimary else if (isDark) TextSecondary else LightTextSecondary,
                    modifier = Modifier.padding(end = 10.dp)
                )
                Column {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isDark) TextPrimary else LightTextPrimary
                    )
                    Text(
                        text = subtitle,
                        fontSize = 10.sp,
                        color = if (isDark) TextMuted else LightTextMuted
                    )
                }
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = PurplePrimary
                )
            }
        }
    }
}

@Composable
fun DrawerToolItem(
    title: String,
    icon: ImageVector,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val bg = if (isDark) Color(0xFF130E26) else Color(0xFFF8FAFC)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        color = bg,
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PurpleSecondary,
                modifier = Modifier.padding(end = 10.dp)
            )
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDark) TextPrimary else LightTextPrimary
            )
        }
    }
}
