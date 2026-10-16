package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.BlackBorder
import com.example.ui.theme.BlackSurface
import com.example.ui.theme.MathAmber
import com.example.ui.theme.MathGreen
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.PurpleSecondary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ScopeDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            colors = CardDefaults.cardColors(containerColor = BlackSurface),
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BlackBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = PurpleAccent,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "راهنما و شفاف‌سازی دامنه ریاضی",
                            color = PurpleAccent,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "بستن", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "اپلیکیشن PUNY4 Calculator با موتور محاسبات نمادین و عددی آفلاین طراحی شده است. مطابق اصول مهندسی و شفافیت علمی، محدوده قابلیت‌های پشتیبانی‌شده و محدودیت‌ها به شرح زیر است:",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )

                    // Supported Features
                    Card(
                        colors = CardDefaults.cardColors(containerColor = BlackSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MathGreen.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = MathGreen, modifier = Modifier.padding(end = 6.dp))
                                Text("قابلیت‌های پشتیبانی‌شده با حل گام‌به‌گام:", color = MathGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            FeatureItem("کسرها و اعداد گویا", "چهارعمل اصلی، ک.م.م، ب.م.م، هم‌مخرج‌سازی و تحویل کسر به ساده‌ترین فرم.")
                            FeatureItem("معادلات درجه ۱ و ۲", "روش کامل دلتا (Δ)، ریشه‌های حقیقی متمایز، مضاعف و مختلط، مختصات رأس سهمی.")
                            FeatureItem("دستگاه دو معادله و دو مجهول", "حل تحلیلی با قاعده دترمینان کرامر (D, Dx, Dy) و بررسی جواب‌های بی‌شمار یا تناقض.")
                            FeatureItem("مشتق‌گیری تحلیلی", "قاعده توان، ضریب ثابت، زنجیره‌ای، توابع مثلثاتی، نمایی e^x، لگاریتم ln(x) و محاسبه شیب در نقطه.")
                            FeatureItem("انتگرال‌گیری معین و نامعین", "پادمشتق F(x) با ثابت C و قضیه اساسی حسابان با کران‌های بالا و پایین [F(b) - F(a)].")
                            FeatureItem("محاسبه حد و رفع ابهام", "تشخیص ابهام صفر بر صفر (0/0)، فاکتورگیری عامل صفرکننده (x - a) و قاعده هوپیتال.")
                            FeatureItem("سیگما و سری‌ها", "بسط متوالی جملات و فرمول بسته گوس برای مجموع اعداد متوالی.")
                            FeatureItem("ماتریس ۲×۲", "محاسبه دترمینان ad-bc، ماتریس ترانهاده، الحاقی و وارون A⁻¹ با شرط تکین بودن.")
                            FeatureItem("محاسبات علمی بزرگ", "دقت محاسباتی بالا (BigDecimal)، فاکتوریل تا ۱۰۰۰!، جایگشت و ترکیب.")
                        }
                    }

                    // Limitations
                    Card(
                        colors = CardDefaults.cardColors(containerColor = BlackSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MathAmber.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = MathAmber, modifier = Modifier.padding(end = 6.dp))
                                Text("موارد پشتیبانی‌نشده و محدودیت‌های موتور آفلاین:", color = MathAmber, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            FeatureItem("معادلات جبری درجه ۵ و بالاتر", "طبق قضیه آبل-روفینی، هیچ فرمول رادیکالی کلی جبری برای این معادلات وجود ندارد.")
                            FeatureItem("انتگرال‌های غیراولیه الپتیک", "توابعی مانند e^(-x²) که فاقد تابع پادمشتق بسته تحلیلی هستند.")
                            FeatureItem("ماتریس‌های ابعاد بسیار بزرگ (بالاتر از ۴×۴)", "به منظور حفظ سرعت و روانی رابط کاربری بدون لگ در پردازش موبایل محدود شده است.")
                            FeatureItem("توابع غیرمداوم با نقاط تکینگی اساسی", "حدود در نقاطی که تابع به شکل نوسانی بی‌نهایت رفتار می‌کند (مانند sin(1/x) در x=0).")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FeatureItem(title: String, desc: String) {
    Column {
        Text(text = "• $title:", color = PurpleSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Text(text = desc, color = TextMuted, fontSize = 11.sp, lineHeight = 16.sp, modifier = Modifier.padding(start = 12.dp))
    }
}
