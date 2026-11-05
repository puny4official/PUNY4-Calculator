package com.example.update

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.R

object UpdateNotifier {
    private const val CHANNEL_ID = "app_updates"
    private const val PREFS = "update_prefs"
    private const val KEY_NOTIFIED = "last_notified_version"

    /** Shows the "new version" notification, once per version. */
    fun notifyNewVersion(context: Context, version: String) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (prefs.getString(KEY_NOTIFIED, null) == version) return
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "به‌روزرسانی برنامه", NotificationManager.IMPORTANCE_DEFAULT)
            )
        }
        val open = PendingIntent.getActivity(
            context,
            0,
            Intent(Intent.ACTION_VIEW, Uri.parse(UpdateChecker.REPO_URL)),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("نسخه جدید PUNY4 Calculator")
            .setContentText("نسخه $version آماده است. برای دیدن صفحه‌ی دانلود بزنید.")
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        manager.notify(1001, notification)
        prefs.edit().putString(KEY_NOTIFIED, version).apply()
    }
}
