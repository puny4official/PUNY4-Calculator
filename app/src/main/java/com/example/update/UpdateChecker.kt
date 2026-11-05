package com.example.update

import java.net.HttpURLConnection
import java.net.URL

/**
 * Looks up the newest app version: the `versionName` written in app/build.gradle.kts
 * on the main branch of the GitHub repository.
 */
object UpdateChecker {
    const val REPO_URL = "https://github.com/puny4official/PUNY4-Calculator"
    private const val VERSION_FILE_URL =
        "https://raw.githubusercontent.com/puny4official/PUNY4-Calculator/main/app/build.gradle.kts"

    /** Blocking network call: run it off the main thread. Returns null when it cannot be read. */
    fun fetchLatestVersion(): String? = try {
        val connection = (URL(VERSION_FILE_URL).openConnection() as HttpURLConnection).apply {
            connectTimeout = 10_000
            readTimeout = 10_000
            requestMethod = "GET"
        }
        try {
            if (connection.responseCode != 200) null
            else {
                val text = connection.inputStream.bufferedReader().use { it.readText() }
                Regex("""versionName\s*=\s*"([^"]+)"""").find(text)?.groupValues?.get(1)
            }
        } finally {
            connection.disconnect()
        }
    } catch (e: Exception) {
        null
    }

    /** True when [latest] is a higher version than [current] (compared number by number). */
    fun isNewer(latest: String, current: String): Boolean {
        val a = latest.split('.').map { it.filter(Char::isDigit).toIntOrNull() ?: 0 }
        val b = current.split('.').map { it.filter(Char::isDigit).toIntOrNull() ?: 0 }
        for (i in 0 until maxOf(a.size, b.size)) {
            val x = a.getOrElse(i) { 0 }
            val y = b.getOrElse(i) { 0 }
            if (x != y) return x > y
        }
        return false
    }
}
