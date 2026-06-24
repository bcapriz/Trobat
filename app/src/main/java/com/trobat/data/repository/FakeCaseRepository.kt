package com.trobat.data.repository

import com.trobat.data.model.MissingPersonCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeCaseRepository : CaseRepository {

    private val _cases = MutableStateFlow(
        listOf(
            MissingPersonCase(
                id = "1",
                fullName = "Martín Domínguez",
                age = 42,
                physicalDescription = "Mide 1.75m, contextura media, cabello corto castaño. Vestía campera azul y jeans oscuros al momento de su desaparición.",
                lastSeenLocation = "Estación de trenes, Wilde",
                lastSeenDate = "24 de Mayo, 18:30 hs",
                area = "Wilde, Buenos Aires",
                latitude = -34.7042,
                longitude = -58.3150
            ),
            MissingPersonCase(
                id = "2",
                fullName = "Sofía Herrera",
                age = 16,
                physicalDescription = "Mide 1.60m, cabello largo rubio, ojos marrones. Llevaba mochila negra escolar.",
                lastSeenLocation = "Av. Mitre y Las Flores, Wilde",
                lastSeenDate = "25 de Mayo, 07:15 hs",
                area = "Wilde, Buenos Aires",
                latitude = -34.6980,
                longitude = -58.3195
            ),
            MissingPersonCase(
                id = "3",
                fullName = "Roberto Carlos Gómez",
                age = 78,
                physicalDescription = "Mide 1.65m, cabello canoso, usa anteojos. Presenta principio de Alzheimer. Vestía pantalón de vestir gris.",
                lastSeenLocation = "Parque Domínico",
                lastSeenDate = "25 de Mayo, 10:00 hs",
                area = "Domínico, Buenos Aires",
                latitude = -34.6865,
                longitude = -58.3300
            )
        )
    )

    override val cases: StateFlow<List<MissingPersonCase>> = _cases.asStateFlow()

    override suspend fun refresh() {}
    override suspend fun refreshCercanos(lat: Double, lng: Double, radioKm: Double) {}
    override suspend fun refreshCercanosConFallback(lat: Double, lng: Double, initialRadioKm: Double) {}
    override suspend fun searchByName(query: String): List<MissingPersonCase> = emptyList()
    override suspend fun cacheCase(case: MissingPersonCase) {}
}
