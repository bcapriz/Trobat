package com.trobat.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.PersonSearch
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trobat.ui.viewmodel.HeatMapUiState
import com.trobat.ui.viewmodel.HeatMapViewModel
import com.trobat.ui.viewmodel.MockMissingPersonCase

@Composable
fun HeatMapScreen(
    viewModel: HeatMapViewModel = viewModel(),
    modifier: Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    HeatMapContent(uiState = uiState)
}

@Composable
private fun HeatMapContent(
    uiState: HeatMapUiState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp)
            .padding(top = 48.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Zonas de búsqueda",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Visualizá las áreas con mayor concentración de casos activos.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        HeatMapMockCard(totalCases = uiState.totalCases)

        HeatMapStatsCard(
            totalCases = uiState.totalCases,
            mostActiveArea = uiState.mostActiveArea
        )

        Text(
            text = "Últimas búsquedas registradas",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.cases.isEmpty()) {
            Text(
                text = "No hay casos activos registrados en esta zona.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            uiState.cases.forEach { caseItem ->
                HeatMapCaseCard(caseItem = caseItem)
            }
        }
    }
}

@Composable
private fun HeatMapMockCard(totalCases: Int) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(360.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(28.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Map,
                contentDescription = "Mapa simulado",
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                modifier = Modifier.size(210.dp)
            )

            HeatPoint(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 60.dp, top = 70.dp),
                intensity = 0.85f
            )

            HeatPoint(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(start = 70.dp),
                intensity = 0.65f
            )

            HeatPoint(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 95.dp, bottom = 82.dp),
                intensity = 0.45f
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.PersonSearch,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )

                Text(
                    text = "Mapa de incidencia",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "$totalCases búsquedas activas",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun HeatPoint(
    modifier: Modifier = Modifier,
    intensity: Float
) {
    Box(
        modifier = modifier
            .size((90 * intensity).dp)
            .background(
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.22f), // Cambiado a error (rojo) para denotar urgencia, o puedes volver a primary
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size((48 * intensity).dp)
                .background(
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.35f),
                    shape = CircleShape
                )
        )
    }
}

@Composable
private fun HeatMapStatsCard(
    totalCases: Int,
    mostActiveArea: String
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Resumen de zona",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatItem(
                    title = "Casos activos",
                    value = totalCases.toString(),
                    modifier = Modifier.weight(1f)
                )

                StatItem(
                    title = "Mayor concentración",
                    value = mostActiveArea,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun HeatMapCaseCard(
    caseItem: MockMissingPersonCase
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.PersonSearch,
                    contentDescription = "Búsqueda activa",
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    text = "${caseItem.fullName}, ${caseItem.age} años",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = caseItem.physicalDescription,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Divider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Place,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Visto por última vez: ${caseItem.lastSeenLocation}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = caseItem.lastSeenDate,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 20.dp)
            )
        }
    }
}