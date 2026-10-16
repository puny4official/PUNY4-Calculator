package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calculations")
data class CalculationRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val mode: String,              // "NORMAL" or "MATH_STUDIO"
    val category: String,          // e.g., "SCIENTIFIC", "FRACTION", "DERIVATIVE", "INTEGRAL", "LIMIT", "MATRIX", "EQUATION", "SIGMA"
    val title: String,             // Human readable title in Persian, e.g., "مشتق چندجمله‌ای"
    val expression: String,        // Input text / formula representation
    val exactResult: String,       // Exact result or simplified formula
    val approxResult: String? = null, // Decimal numerical result if applicable
    val stepsJson: String = "",    // JSON serialized list of steps
    val isFavorite: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
