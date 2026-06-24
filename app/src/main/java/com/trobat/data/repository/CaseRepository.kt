package com.trobat.data.repository

import com.trobat.data.model.MissingPersonCase
import kotlinx.coroutines.flow.StateFlow

interface CaseRepository {
    val cases: StateFlow<List<MissingPersonCase>>
    suspend fun refresh() {}
    suspend fun refreshCercanos(lat: Double, lng: Double, radioKm: Double = 50.0) {}
    suspend fun refreshCercanosConFallback(lat: Double, lng: Double, initialRadioKm: Double = 50.0) {}
    suspend fun searchByName(query: String): List<MissingPersonCase> = emptyList()
    suspend fun cacheCase(case: MissingPersonCase) {}
}
