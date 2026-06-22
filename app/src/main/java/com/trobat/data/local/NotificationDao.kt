package com.trobat.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY receivedAt DESC")
    fun observeAll(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Int)

    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0")
    fun observeUnreadCount(): Flow<Int>
}
