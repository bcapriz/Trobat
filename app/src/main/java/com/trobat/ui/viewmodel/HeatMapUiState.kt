package com.trobat.ui.viewmodel

import android.location.Location

data class HeatMapUiState(
    val cases: List<MockMissingPersonCase> = emptyList(),
    val totalCases: Int = 0,
    val mostActiveArea: String = "-",
    val isLoading: Boolean = true
)

data class MockMissingPersonCase(
    val fullName: String,
    val age: Int,
    val physicalDescription: String,
    val lastSeenLocation: String,
    val lastSeenDate: String,
    val latitude: Double,
    val longitude: Double
)