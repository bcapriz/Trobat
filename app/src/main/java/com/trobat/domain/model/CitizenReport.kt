package com.trobat.domain.model

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
    val status: ReportStatus
)

enum class ReportStatus {
    NEW,
    SENT
}
