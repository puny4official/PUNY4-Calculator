package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.entity.CalculationRecord
import com.example.ui.CalculatorViewModel
import com.example.ui.theme.*

@Composable
fun HistoryDialog(
    viewModel: CalculatorViewModel,
    onDismiss: () -> Unit
) {
    val historyList by viewModel.historyList.collectAsState(initial = emptyList())
    val themeMode by viewModel.themeMode.collectAsState()
    val isDark = themeMode == AppThemeMode.AMOLED_DARK

    val bg = if (isDark) AmoledDisplaySurface else LightDisplaySurface
    val primaryText = if (isDark) TextPrimary else LightTextPrimary
    val secondaryText = if (isDark) TextSecondary else LightTextSecondary
    val borderCol = if (isDark) BlackBorder else LightBorder

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = bg),
            border = androidx.compose.foundation.BorderStroke(1.dp, borderCol),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.History, contentDescription = null, tint = PurplePrimary)
                        Text(
                            text = "تاریخچه محاسبات",
                            color = primaryText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "بستن", tint = secondaryText)
                    }
                }

                Divider(color = borderCol, modifier = Modifier.padding(vertical = 8.dp))

                // List
                if (historyList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "هنوز محاسبه‌ای ذخیره نشده است.",
                            color = secondaryText,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(historyList) { item ->
                            HistoryItemCard(
                                item = item,
                                isDark = isDark,
                                onRestore = { viewModel.restoreCalculation(item) }
                            )
                        }
                    }
                }

                // Footer (Clear button)
                if (historyList.isNotEmpty()) {
                    Divider(color = borderCol, modifier = Modifier.padding(vertical = 8.dp))
                    Button(
                        onClick = { viewModel.clearHistory() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDark) AmoledKeyClearBg else LightKeyClearBg,
                            contentColor = if (isDark) AmoledKeyClearText else LightKeyClearText
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "پاک کردن کل تاریخچه", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryItemCard(
    item: CalculationRecord,
    isDark: Boolean,
    onRestore: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) AmoledBackground else LightBackground
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isDark) AmoledPillBadgeBorder else LightPillBadgeBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onRestore() }
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isDark) AmoledPillBadgeBg else LightPillBadgeBg)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = item.categoryFa,
                        color = PurpleSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = item.mode,
                    color = PurplePrimary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Text(
                text = item.expression,
                color = if (isDark) TextSecondary else LightTextSecondary,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace
            )

            Text(
                text = "= ${item.exactResult}",
                color = MathGreen,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
