package com.trobat.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.trobat.data.model.MissingPersonCase
import com.trobat.ui.viewmodel.HeatMapUiState
import com.trobat.ui.viewmodel.HeatMapViewModel

@Composable
fun HeatMapScreen(
    modifier: Modifier = Modifier,
    viewModel: HeatMapViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    HeatMapContent(
        uiState = uiState,
        modifier = modifier
    )
}

@Composable
private fun HeatMapContent(
    uiState: HeatMapUiState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 22.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 24.dp)
    ) {
        item {
            Text(
                text = "Zonas de búsqueda",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Text(
                text = "Visualizá las áreas con mayor concentración de casos activos.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            HeatMapCard(cases = uiState.cases)
        }

        item {
            HeatMapStatsCard(
                totalCases = uiState.totalCases,
                mostActiveArea = uiState.mostActiveArea
            )
        }

        item {
            Text(
                text = "Últimas búsquedas registradas",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
        }

        if (uiState.isLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        } else if (uiState.cases.isEmpty()) {
            item {
                Text(
                    text = "No hay casos activos registrados en esta zona.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(uiState.cases) { caseItem ->
                HeatMapCaseCard(caseItem = caseItem)
            }
        }
    }
}

@Composable
private fun HeatMapCard(
    cases: List<MissingPersonCase>
) {
    val firstCase = cases.firstOrNull()
    // Si hay casos, centra la cámara en el primero. Si no, centra por defecto en Wilde/Avellaneda.
    val initialPosition = if (firstCase != null) {
        LatLng(firstCase.latitude, firstCase.longitude)
    } else {
        LatLng(-34.6980, -58.3195)
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialPosition, 14f)
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(360.dp),
        shape = RoundedCornerShape(28.dp)
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
        ) {
            cases.forEach { caseItem ->
                Marker(
                    state = MarkerState(
                        position = LatLng(caseItem.latitude, caseItem.longitude)
                    ),
                    title = caseItem.fullName,
                    snippet = "Última vez visto: ${caseItem.lastSeenLocation}"
                )
            }
        }
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
    caseItem: MissingPersonCase
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

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

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
