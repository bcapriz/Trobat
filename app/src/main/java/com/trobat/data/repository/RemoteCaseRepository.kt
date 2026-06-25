package com.trobat.data.repository

import com.trobat.data.local.db.TrobatDatabase
import com.trobat.data.local.db.entity.toDomain
import com.trobat.data.local.db.entity.toEntity
import com.trobat.data.remote.TrobatApi
import com.trobat.data.repository.mapper.toDomain
import com.trobat.data.model.MissingPersonCase
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val TAG = "RemoteCaseRepository"

class RemoteCaseRepository(
    private val api: TrobatApi,
    private val db: TrobatDatabase
) : CaseRepository {

    private val _cases = MutableStateFlow<List<MissingPersonCase>>(emptyList())
    override val cases: StateFlow<List<MissingPersonCase>> = _cases.asStateFlow()

    override suspend fun refresh() {
        try {
            val response = api.getCasos()
            if (response.isSuccessful) {
                val cases = response.body()?.data?.map { it.toDomain() } ?: emptyList()
                _cases.value = cases
                if (cases.isNotEmpty()) saveToCache(cases)
                return
            }
        } catch (e: Exception) {
            Log.w(TAG, "refresh failed", e)
        }
        loadFromCache()
    }

    override suspend fun refreshCercanos(lat: Double, lng: Double, radioKm: Double) {
        try {
            val response = api.getCasosCercanos(lat = lat, lng = lng, radioKm = radioKm)
            if (response.isSuccessful) {
                val cases = response.body()?.data?.map { it.caso.toDomain() } ?: emptyList()
                _cases.value = cases
                if (cases.isNotEmpty()) saveToCache(cases)
                return
            }
        } catch (e: Exception) {
            Log.w(TAG, "refreshCercanos failed", e)
        }
        if (_cases.value.isEmpty()) loadFromCache()
    }

    override suspend fun refreshCercanosConFallback(lat: Double, lng: Double, initialRadioKm: Double) {
        val pasos = buildRadiusSteps(initialRadioKm)
        for (radio in pasos) {
            refreshCercanos(lat, lng, radio)
            if (_cases.value.isNotEmpty()) return
        }
        refresh()
    }

    override suspend fun searchByName(query: String): List<MissingPersonCase> {
        return try {
            val response = api.buscarCasos(q = query)
            if (response.isSuccessful) {
                response.body()?.data?.map { it.toDomain() } ?: emptyList()
            } else emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun buildRadiusSteps(initialKm: Double): List<Double> {
        if (initialKm >= 100.0) return listOf(100.0)
        val step = (100.0 - initialKm) / 4.0
        return (0..4).map { (initialKm + it * step).coerceAtMost(100.0) }
    }

    override suspend fun cacheCase(case: MissingPersonCase) {
        db.caseDao().insertAll(listOf(case.toEntity()))
        if (_cases.value.none { it.id == case.id }) {
            _cases.value = _cases.value + case
        }
    }

    private suspend fun saveToCache(cases: List<MissingPersonCase>) {
        db.caseDao().deleteAll()
        db.caseDao().insertAll(cases.map { it.toEntity() })
    }

    private suspend fun loadFromCache() {
        val cached = db.caseDao().getAll().map { it.toDomain() }
        if (cached.isNotEmpty()) _cases.value = cached
    }
}
