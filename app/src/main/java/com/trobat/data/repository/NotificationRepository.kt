package com.trobat.data.repository

import com.trobat.data.local.NotificationDao
import com.trobat.data.local.NotificationEntity
import kotlinx.coroutines.flow.Flow

class NotificationRepository(private val dao: NotificationDao) {

    fun observeAll(): Flow<List<NotificationEntity>> = dao.observeAll()

    fun observeUnreadCount(): Flow<Int> = dao.observeUnreadCount()

    suspend fun save(title: String, body: String, id: Int) {
        dao.insert(
            NotificationEntity(
                id = id,
                title = title,
                body = body,
                receivedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun markAsRead(id: Int) = dao.markAsRead(id)
}
