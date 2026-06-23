package com.trobat.data.model

import android.net.Uri

object CapturedEvidenceHolder {
    var photoUri: Uri? = null
    var latitude: Double? = null
    var longitude: Double? = null
    var preselectedCaseId: String? = null

    fun clear() {
        photoUri = null
        latitude = null
        longitude = null
        preselectedCaseId = null
    }
}
