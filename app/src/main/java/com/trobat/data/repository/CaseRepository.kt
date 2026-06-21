package com.trobat.data.repository

import com.trobat.data.model.MissingPersonCase
import kotlinx.coroutines.flow.StateFlow

interface CaseRepository {
    val cases: StateFlow<List<MissingPersonCase>>
    suspend fun refreshCercanos(lat: Double, lng: Double, radioKm: Double = 50.0) {}
}
