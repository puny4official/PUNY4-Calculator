package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.CalculatorViewModel
import com.example.ui.theme.*

@Composable
fun ScopeDialog(
    viewModel: CalculatorViewModel,
    onDismiss: () -> Unit
) {
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
                        Icon(Icons.Default.Info, contentDescription = null, tint = PurplePrimary)
                        Text(
                            text = "دامنه و قابلیت‌های موتور ریاضی",
                            color = primaryText,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "بستن", tint = secondaryText)
                    }
                }

                Divider(color = borderCol, modifier = Modifier.padding(vertical = 8.dp))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = if (isDark) AmoledBackground else LightBackground),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MathGreen, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("قابلیت‌های پشتیبانی‌شده (آفلاین)", color = MathGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                                Text("• محاسبات علمی با دقت فوق‌العاده بالا تا ۳۶ رقم معنادار", color = secondaryText, fontSize = 12.5.sp)
                                Text("• تفکیک سه‌رقمی اعداد با کاما ( ، ) و تلفظ حروفی فارسی عدد", color = secondaryText, fontSize = 12.5.sp)
                                Text("• محاسبات چهار عمل اصلی کسرها با ک.م.م و ب.م.م مرحله‌به‌مرحله", color = secondaryText, fontSize = 12.5.sp)
                                Text("• حل تحلیلی معادلات درجه دوم با بررسی علامت دلتا (حقیقی و مختلط)", color = secondaryText, fontSize = 12.5.sp)
                                Text("• مشتق توابع چندجمله‌ای با قاعده توان و مجموع و محاسبه در نقطه", color = secondaryText, fontSize = 12.5.sp)
                                Text("• انتگرال معین و نامعین با تابع پادمشتق و قضیه اساسی حسابان", color = secondaryText, fontSize = 12.5.sp)
                                Text("• رفع ابهام حد صفر بر صفر با اتحادهای جبری مزدوج", color = secondaryText, fontSize = 12.5.sp)
                                Text("• سری و سیگما با روابط گاوس و فرمول مجموع مربعات", color = secondaryText, fontSize = 12.5.sp)
                                Text("• دترمینان، وارون و دستگاه دو معادله خطی به روش کرامر", color = secondaryText, fontSize = 12.5.sp)
                            }
                        }
                    }

                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = if (isDark) AmoledKeyClearBg else LightKeyClearBg),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = MathPink, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("محدودیت‌ها و موارد پشتیبانی‌نشده", color = if (isDark) AmoledKeyClearText else LightKeyClearText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                                Text("• انتگرال توابع غیرمقدماتی (مانند انتگرال گوسی e^(-x²)) فاقد پادمشتق بسته تحلیلی هستند.", color = secondaryText, fontSize = 12.5.sp)
                                Text("• معادلات چندجمله‌ای درجه ۵ و بالاتر به دلیل قضیه آبل-روفینی فاقد فرمول رادیکالی عام هستند.", color = secondaryText, fontSize = 12.5.sp)
                                Text("• معادلات دیفرانسیل جزئی غیرخطی در بستر آفلاین موبایل پشتیبانی نمی‌شوند.", color = secondaryText, fontSize = 12.5.sp)
                            }
                        }
                    }
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("متوجه شدم", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
