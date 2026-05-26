package com.trobat.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HeatMapViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HeatMapUiState())
    val uiState: StateFlow<HeatMapUiState> = _uiState.asStateFlow()

    init {
        loadMockedData()
    }

    private fun loadMockedData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            delay(1000)

            val mockCases = listOf(
                MockMissingPersonCase(
                    fullName = "Martín Domínguez",
                    age = 42,
                    physicalDescription = "Mide 1.75m, contextura media, cabello corto castaño. Vestía campera azul y jeans oscuros al momento de su desaparición.",
                    lastSeenLocation = "Estación de trenes, Wilde",
                    lastSeenDate = "24 de Mayo, 18:30 hs",
                    latitude = -34.7042,
                    longitude = -58.3150
                ),
                MockMissingPersonCase(
                    fullName = "Sofía Herrera",
                    age = 16,
                    physicalDescription = "Mide 1.60m, cabello largo rubio, ojos marrones. Llevaba mochila negra escolar.",
                    lastSeenLocation = "Av. Mitre y Las Flores, Wilde",
                    lastSeenDate = "25 de Mayo, 07:15 hs",
                    latitude = -34.6980,
                    longitude = -58.3195
                ),
                MockMissingPersonCase(
                    fullName = "Roberto Carlos Gómez",
                    age = 78,
                    physicalDescription = "Mide 1.65m, cabello canoso, usa anteojos. Presenta principio de Alzheimer. Vestía pantalón de vestir gris.",
                    lastSeenLocation = "Parque Domínico",
                    lastSeenDate = "25 de Mayo, 10:00 hs",
                    latitude = -34.6865,
                    longitude = -58.3300
                )
            )

            _uiState.value = HeatMapUiState(
                cases = mockCases,
                totalCases = mockCases.size,
                mostActiveArea = "Wilde Centro",
                isLoading = false
            )
        }
    }
}