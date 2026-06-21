package com.trobat.data.repository

import com.trobat.data.model.MissingPersonCase
import com.trobat.data.remote.TrobatApi
import com.trobat.data.remote.dto.CasoDto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RemoteCaseRepository(private val api: TrobatApi) : CaseRepository {

    private val _cases = MutableStateFlow<List<MissingPersonCase>>(emptyList())
    override val cases: StateFlow<List<MissingPersonCase>> = _cases.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        scope.launch { fetchCases() }
    }

    suspend fun refresh() = fetchCases()

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

private fun CasoDto.toDomain(): MissingPersonCase {
    val coords = missing_person.last_known_location?.coordinates
    val lat = coords?.getOrNull(1) ?: 0.0
    val lng = coords?.getOrNull(0) ?: 0.0
    val location = (missing_person.location_label ?: missing_person.location_description).ifBlank {
        if (lat != 0.0) "Lat: ${"%.5f".format(lat)}, Lng: ${"%.5f".format(lng)}" else ""
    }
    return MissingPersonCase(
        id = id,
        fullName = missing_person.name,
        age = missing_person.age,
        physicalDescription = missing_person.description,
        lastSeenLocation = location,
        lastSeenDate = missing_person.last_seen_date.ifBlank { created_at },
        area = status,
        latitude = lat,
        longitude = lng
    )
}
