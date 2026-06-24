package com.trobat.data.repository

import androidx.room.withTransaction
import com.trobat.data.TWO_DAYS_MS
import com.trobat.data.local.NotificationEntity
import com.trobat.data.local.TrobatDatabase
import kotlinx.coroutines.flow.Flow

private const val MAX_NOTIFICATIONS = 10

class NotificationRepository(private val db: TrobatDatabase) {

    private val dao = db.notificationDao()

    fun observeAll(): Flow<List<NotificationEntity>> = dao.observeAll()

    fun observeUnreadCount(): Flow<Int> = dao.observeUnreadCount()

    suspend fun save(title: String, body: String, id: Int) {
        val twoDaysAgo = System.currentTimeMillis() - TWO_DAYS_MS
        db.withTransaction {
            dao.deleteOlderThan(twoDaysAgo)
            if (dao.count() >= MAX_NOTIFICATIONS) dao.deleteOldest()
            dao.insert(
                NotificationEntity(
                    id = id,
                    title = title,
                    body = body,
                    receivedAt = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun markAsRead(id: Int) = dao.markAsRead(id)

    suspend fun markAllRead() = dao.markAllRead()
}
