package com.trobat.ui.notifications

import com.trobat.data.local.PendingReportEntity

data class PendingReportItem(
    val entity: PendingReportEntity,
    val caseName: String?
)
