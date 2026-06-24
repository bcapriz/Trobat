package com.trobat.ui.main

import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import com.trobat.R

enum class CoachmarkStep(@StringRes val titleRes: Int, @StringRes val descriptionRes: Int) {
    CASES(R.string.coachmark_cases_title, R.string.coachmark_cases_description),
    CAMERA(R.string.coachmark_camera_title, R.string.coachmark_camera_description),
    HEATMAP(R.string.coachmark_map_title, R.string.coachmark_map_description),
    DONE(0, 0);

    val isLast: Boolean get() = this == HEATMAP

    fun next(): CoachmarkStep = when (this) {
        CASES -> CAMERA
        CAMERA -> HEATMAP
        HEATMAP -> DONE
        DONE -> DONE
    }
}

class CoachmarkController {
    var currentStep by mutableStateOf(CoachmarkStep.CASES)
        private set

    val casesBounds = mutableStateOf<Rect?>(null)
    val heatmapBounds = mutableStateOf<Rect?>(null)
    val cameraBounds = mutableStateOf<Rect?>(null)

    val currentBounds: Rect?
        get() = when (currentStep) {
            CoachmarkStep.CASES -> casesBounds.value
            CoachmarkStep.CAMERA -> cameraBounds.value
            CoachmarkStep.HEATMAP -> heatmapBounds.value
            CoachmarkStep.DONE -> null
        }

    fun advance() {
        currentStep = currentStep.next()
    }

    fun dismiss() {
        currentStep = CoachmarkStep.DONE
    }
}
