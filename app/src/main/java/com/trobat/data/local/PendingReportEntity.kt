package com.trobat.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_reports")
data class PendingReportEntity(
    @PrimaryKey val id: String,
    val caseId: String,
    val description: String,
    val optionalDetails: String?,
    val address: String,
    val createdAt: String,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val latitude: Double,
    val longitude: Double,
    val isAnonymous: Boolean,
    val localPhotoPath: String?,
    val status: String = "PENDING_SYNC"
)
