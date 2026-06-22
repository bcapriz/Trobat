package com.trobat.data.model

data class CitizenReport(
    val id: String,
    val caseId: String,
    val title: String,
    val description: String,
    val optionalDetails: String?,
    val address: String,
    val createdAt: String,
    val latitude: Double,
    val longitude: Double,
    val isAnonymous: Boolean = true,
    val contactName: String? = null,
    val contactPhone: String? = null,
    val contactEmail: String? = null,
    val status: ReportStatus = ReportStatus.NEW
)

enum class ReportStatus {
    NEW,
    SENT,
    PENDING_SYNC
}
