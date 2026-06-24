package com.trobat.ui.viewmodel

sealed interface CitizenHomeEffect {
    data object NavigateToMap : CitizenHomeEffect
    data object NavigateToCamera : CitizenHomeEffect
    data object NavigateToConfirmReport : CitizenHomeEffect
}