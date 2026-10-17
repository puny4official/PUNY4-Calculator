package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calculations")
data class CalculationRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val mode: String,          // "NORMAL" or "STUDIO"
    val categoryFa: String,    // "محاسبه عادی", "مشتق", "انتگرال", "کسر", etc.
    val expression: String,    // Input formula/expression text
    val exactResult: String,   // Exact result
    val approxResult: String?, // Approximate result if different
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)
