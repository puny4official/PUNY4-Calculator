package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.CalculationDao
import com.example.data.entity.CalculationRecord

@Database(entities = [CalculationRecord::class], version = 1, exportSchema = false)
abstract class Puny4Database : RoomDatabase() {
    abstract fun calculationDao(): CalculationDao

    companion object {
        @Volatile
        private var INSTANCE: Puny4Database? = null

        fun getInstance(context: Context): Puny4Database {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    Puny4Database::class.java,
                    "puny4_calculator.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
