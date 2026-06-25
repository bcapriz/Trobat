package com.trobat.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.trobat.data.local.db.entity.PendingReportEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingReportDao {
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(report: PendingReportEntity)

    @Query("SELECT * FROM pending_reports ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<PendingReportEntity>>

    @Query("SELECT * FROM pending_reports")
    suspend fun getAll(): List<PendingReportEntity>

    @Query("SELECT * FROM pending_reports WHERE id = :id")
    suspend fun getById(id: String): PendingReportEntity?

    @Query("DELETE FROM pending_reports WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM pending_reports WHERE status = 'SENT' AND createdAtMillis < :cutoff")
    suspend fun deleteSentOlderThan(cutoff: Long)

    @Query("UPDATE pending_reports SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)

    @Query("UPDATE pending_reports SET status = :status, localPhotoPath = NULL WHERE id = :id")
    suspend fun markSent(id: String, status: String)

    @Query("UPDATE pending_reports SET status = 'PENDING_SYNC' WHERE status = 'SENDING'")
    suspend fun resetSendingToPending()

    @Query("DELETE FROM pending_reports")
    suspend fun deleteAll()
}