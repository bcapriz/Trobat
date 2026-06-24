package com.trobat.ui.heatmap

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.android.gms.maps.CameraUpdateFactory
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
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
import com.trobat.ui.components.ActiveCaseCard
import com.trobat.ui.components.CaseDetailSheet
import com.trobat.ui.components.RadiusSlider

@Composable
fun HeatMapScreen(
    onNavigateToCamera: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: HeatMapViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                HeatMapEffect.NavigateToCamera -> onNavigateToCamera()
            }
        }
    }

    HeatMapContent(
        uiState = uiState,
        onCaseClicked = viewModel::onCaseClicked,
        onDismissCaseModal = viewModel::onDismissCaseModal,
        onRadiusChanged = viewModel::onRadiusChanged,
        onCargarReporte = viewModel::onCargarReporte,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HeatMapContent(
    uiState: HeatMapUiState,
    onCaseClicked: (MissingPersonCase) -> Unit,
    onDismissCaseModal: () -> Unit,
    onRadiusChanged: (Float) -> Unit,
    onCargarReporte: (MissingPersonCase) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val maxMapHeightPx = with(density) { (360.dp + 24.dp).toPx() }
    var mapHeightPx by remember { mutableFloatStateOf(maxMapHeightPx) }

    // Collapse map on scroll-up; expand after list bounces back on scroll-down
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                if (delta < 0f) {
                    val newH = (mapHeightPx + delta).coerceIn(0f, maxMapHeightPx)
                    val consumed = newH - mapHeightPx
                    mapHeightPx = newH
                    return Offset(0f, consumed)
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                val delta = available.y
                if (delta > 0f) {
                    val newH = (mapHeightPx + delta).coerceIn(0f, maxMapHeightPx)
                    val expanded = newH - mapHeightPx
                    mapHeightPx = newH
                    return Offset(0f, expanded)
                }
                return Offset.Zero
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
    ) {
        HeatMapCard(
            cases = uiState.filteredCases,
            userLat = uiState.userLat,
            userLng = uiState.userLng,
            modifier = Modifier
                .fillMaxWidth()
                .height(with(density) { mapHeightPx.toDp() })
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
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
                HeatMapStatsCard(
                    totalCases = uiState.totalCases,
                    mostActiveArea = uiState.mostActiveArea,
                    mostActiveCount = uiState.mostActiveCount
                )
            }

            item {
                Text(
                    text = "Casos activos",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
            }

            if (uiState.userLat != null) {
                item {
                    RadiusSlider(
                        radiusKm = uiState.radiusKm,
                        onRadiusChanged = onRadiusChanged
                    )
                }
            }

            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (uiState.filteredCases.isEmpty()) {
                item {
                    Text(
                        text = if (uiState.cases.isEmpty())
                            "No hay casos activos registrados."
                        else
                            "No hay casos activos en un radio de ${uiState.radiusKm.toInt()} km.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(uiState.filteredCases) { caseItem ->
                    ActiveCaseCard(
                        case = caseItem,
                        distanceKm = uiState.distanceTo(caseItem),
                        onClick = { onCaseClicked(caseItem) }
                    )
                }
            }
        }
    }

    if (uiState.selectedCase != null) {
        ModalBottomSheet(
            onDismissRequest = onDismissCaseModal,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            CaseDetailSheet(
                case = uiState.selectedCase,
                onCargarReporte = { onCargarReporte(uiState.selectedCase) }
            )
        }
    }
}

@Composable
private fun HeatMapCard(
    cases: List<MissingPersonCase>,
    userLat: Double? = null,
    userLng: Double? = null,
    modifier: Modifier = Modifier
) {
    val fallbackPosition =
        cases.firstOrNull()?.let { LatLng(it.latitude, it.longitude) }
            ?: LatLng(-34.6980, -58.3195)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(fallbackPosition, 13f)
    }

    LaunchedEffect(userLat, userLng) {
        if (userLat != null && userLng != null) {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(LatLng(userLat, userLng), 13f)
            )
        }
    }

    ElevatedCard(
        modifier = modifier
            .padding(top = 24.dp)
            .padding(horizontal = 22.dp),
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
    mostActiveArea: String,
    mostActiveCount: Int
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
                    value = if (mostActiveCount > 0) "$mostActiveArea ($mostActiveCount)" else mostActiveArea,
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
