package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.CalculationRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface CalculationDao {
    @Query("SELECT * FROM calculations ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<CalculationRecord>>

    @Query("SELECT * FROM calculations WHERE mode = :mode ORDER BY timestamp DESC")
    fun getHistoryByMode(mode: String): Flow<List<CalculationRecord>>

    @Query("SELECT * FROM calculations WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteFormulas(): Flow<List<CalculationRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalculation(record: CalculationRecord): Long

    @Update
    suspend fun updateCalculation(record: CalculationRecord)

    @Delete
    suspend fun deleteCalculation(record: CalculationRecord)

    @Query("UPDATE calculations SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean)

    @Query("DELETE FROM calculations WHERE isFavorite = 0")
    suspend fun clearNonFavoriteHistory()

    @Query("DELETE FROM calculations")
    suspend fun clearAll()
}
