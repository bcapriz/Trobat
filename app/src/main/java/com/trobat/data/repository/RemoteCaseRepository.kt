package com.trobat.data.repository

import com.trobat.data.remote.TrobatApi
import com.trobat.data.repository.mapper.toDomain
import com.trobat.data.model.MissingPersonCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RemoteCaseRepository(
    private val api: TrobatApi,
    private val scope: CoroutineScope
) : CaseRepository {

    private val _cases = MutableStateFlow<List<MissingPersonCase>>(emptyList())
    override val cases: StateFlow<List<MissingPersonCase>> = _cases.asStateFlow()

    override suspend fun refresh() = fetchCases()

    override suspend fun refreshCercanos(lat: Double, lng: Double, radioKm: Double) {
        try {
            val response = api.getCasosCercanos(lat = lat, lng = lng, radioKm = radioKm)
            if (response.isSuccessful) {
                _cases.value = response.body()?.data?.map { it.caso.toDomain() } ?: emptyList()
            }
        } catch (_: Exception) {
        }
    }

    override suspend fun refreshCercanosConFallback(lat: Double, lng: Double, initialRadioKm: Double) {
        val pasos = buildRadiusSteps(initialRadioKm)
        for (radio in pasos) {
            refreshCercanos(lat, lng, radio)
            if (_cases.value.isNotEmpty()) return
        }
        refresh()
    }

    private fun buildRadiusSteps(initialKm: Double): List<Double> {
        if (initialKm >= 100.0) return listOf(100.0)
        val step = (100.0 - initialKm) / 4.0
        return (0..4).map { (initialKm + it * step).coerceAtMost(100.0) }
    }

    private suspend fun fetchCases() {
        try {
            val response = api.getCasos()
            if (response.isSuccessful) {
                _cases.value = response.body()?.data?.map { it.toDomain() } ?: emptyList()
            }
        } catch (_: Exception) {
        }
    }
}
