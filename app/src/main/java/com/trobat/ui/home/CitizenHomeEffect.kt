package com.trobat.ui.home

sealed interface CitizenHomeEffect {
    data object NavigateToMap : CitizenHomeEffect
    data object NavigateToCamera : CitizenHomeEffect
    data object NavigateToConfirmReport : CitizenHomeEffect
}