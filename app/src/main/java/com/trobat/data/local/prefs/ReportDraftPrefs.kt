package com.trobat.data.local.prefs

import android.content.Context
import android.net.Uri
import java.io.File

class ReportDraftPrefs(context: Context) {

    private val prefs = context.getSharedPreferences("report_draft", Context.MODE_PRIVATE)

    data class Draft(
        val photoUriString: String? = null,
        val latitude: Double? = null,
        val longitude: Double? = null,
        val locationLabel: String = "",
        val selectedCaseId: String? = null,
        val selectedCaseName: String? = null,
        val description: String = "",
        val details: String = "",
        val isIdentified: Boolean = false
    ) {
        val photoUri: Uri? get() = photoUriString?.let {
            try {
                val uri = Uri.parse(it)
                // file:// URIs — verify file still exists
                if (uri.scheme == "file") {
                    val file = File(uri.path ?: return@let null)
                    if (file.exists()) uri else null
                } else uri
            } catch (_: Exception) { null }
        }
    }

    fun save(draft: Draft) {
        prefs.edit().apply {
            if (draft.photoUriString != null) putString("photo_uri", draft.photoUriString) else remove("photo_uri")
            if (draft.latitude != null) putLong("lat", java.lang.Double.doubleToRawLongBits(draft.latitude)) else remove("lat")
            if (draft.longitude != null) putLong("lng", java.lang.Double.doubleToRawLongBits(draft.longitude)) else remove("lng")
            putString("location_label", draft.locationLabel)
            if (draft.selectedCaseId != null) putString("case_id", draft.selectedCaseId) else remove("case_id")
            if (draft.selectedCaseName != null) putString("case_name", draft.selectedCaseName) else remove("case_name")
            putString("description", draft.description)
            putString("details", draft.details)
            putBoolean("is_identified", draft.isIdentified)
        }.apply()
    }

    fun load(): Draft = Draft(
        photoUriString = prefs.getString("photo_uri", null),
        latitude = if (prefs.contains("lat")) java.lang.Double.longBitsToDouble(prefs.getLong("lat", 0L)) else null,
        longitude = if (prefs.contains("lng")) java.lang.Double.longBitsToDouble(prefs.getLong("lng", 0L)) else null,
        locationLabel = prefs.getString("location_label", "") ?: "",
        selectedCaseId = prefs.getString("case_id", null),
        selectedCaseName = prefs.getString("case_name", null),
        description = prefs.getString("description", "") ?: "",
        details = prefs.getString("details", "") ?: "",
        isIdentified = prefs.getBoolean("is_identified", false)
    )

    fun isEmpty(): Boolean = prefs.getString("photo_uri", null) == null &&
            prefs.getString("description", "").isNullOrBlank() &&
            prefs.getString("case_id", null) == null

    fun clear() = prefs.edit().clear().apply()
}