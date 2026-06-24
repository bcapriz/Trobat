package com.trobat.ui.screen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect

enum class CoachmarkStep(val title: String, val description: String) {
    CASES(
        title = "Casos activos",
        description = "Explorá las personas buscadas cercanas a tu ubicación y sus últimas novedades."
    ),
    CAMERA(
        title = "Reportar avistamiento",
        description = "Sacá una foto y enviá tu reporte con ubicación exacta en pocos segundos."
    ),
    HEATMAP(
        title = "Mapa de calor",
        description = "Visualizá las zonas con mayor concentración de reportes en tu ciudad."
    ),
    DONE("", "");

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
