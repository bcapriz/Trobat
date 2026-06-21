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
