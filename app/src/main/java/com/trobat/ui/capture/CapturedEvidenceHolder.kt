package com.trobat.ui.capture

import android.net.Uri
import java.io.File

object CapturedEvidenceHolder {
    var photoUri: Uri? = null
    var latitude: Double? = null
    var longitude: Double? = null
    var preselectedCaseId: String? = null
    var localFilePath: String? = null

    fun clear() {
        localFilePath?.let { File(it).delete() }
        photoUri = null
        latitude = null
        longitude = null
        preselectedCaseId = null
        localFilePath = null
    }
}
