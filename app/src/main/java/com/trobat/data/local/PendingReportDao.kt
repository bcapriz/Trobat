package com.trobat.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingReportDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(report: PendingReportEntity)

    @Query("SELECT * FROM pending_reports ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<PendingReportEntity>>

    @Query("SELECT * FROM pending_reports")
    suspend fun getAll(): List<PendingReportEntity>

    @Query("UPDATE pending_reports SET status = 'SENT', localPhotoPath = NULL WHERE id = :id")
    suspend fun markAsSent(id: String)

    @Query("DELETE FROM pending_reports WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM pending_reports WHERE status = 'SENT' AND createdAtMillis < :cutoff")
    suspend fun deleteSentOlderThan(cutoff: Long)
}
