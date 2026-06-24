package com.trobat.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val body: String,
    val receivedAt: Long,
    val isRead: Boolean = false
)
