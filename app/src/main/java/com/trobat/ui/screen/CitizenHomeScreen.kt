package com.trobat.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trobat.data.model.MissingPersonCase
import com.trobat.ui.components.ActiveCaseCard
import com.trobat.ui.components.CaseDetailSheet
import com.trobat.ui.viewmodel.CitizenHomeEffect
import com.trobat.ui.viewmodel.CitizenHomeEvent
import com.trobat.ui.viewmodel.CitizenHomeUiState
import com.trobat.ui.viewmodel.CitizenHomeViewModel
import kotlin.math.roundToInt

@Composable
fun CitizenHomeScreen(
    onOpenMap: () -> Unit = {},
    onCaptureEvidence: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: CitizenHomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                      permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        hasLocationPermission = granted
        if (granted) viewModel.onEvent(CitizenHomeEvent.RefreshClicked)
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            locationPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
        viewModel.effect.collect { effect ->
            when (effect) {
                CitizenHomeEffect.NavigateToMap -> onOpenMap()
                CitizenHomeEffect.NavigateToCamera -> onCaptureEvidence()
            }
        }
    }

    CitizenHomeContent(
        uiState = uiState,
        hasLocationPermission = hasLocationPermission,
        onRequestLocationPermission = {
            locationPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        },
        onEvent = viewModel::onEvent,
        onCargarReporte = { caseId ->
            com.trobat.data.model.CapturedEvidenceHolder.preselectedCaseId = caseId
            onCaptureEvidence()
        },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CitizenHomeContent(
    uiState: CitizenHomeUiState,
    hasLocationPermission: Boolean,
    onRequestLocationPermission: () -> Unit,
    onEvent: (CitizenHomeEvent) -> Unit,
    onCargarReporte: (caseId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp)
            .padding(top = 48.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "TROBAT",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = uiState.title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Podés consultar reportes cercanos o aportar evidencia de forma anónima.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        CollaborationOptionCard(
            title = "Mapa de reportes",
            description = "Consultá zonas activas y reportes cercanos.",
            label = "MAPA",
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Map,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            onClick = { onEvent(CitizenHomeEvent.OpenMapClicked) }
        )

        CollaborationOptionCard(
            title = "Reportar evidencia",
            description = "Tomá una foto y adjuntá una descripción para colaborar.",
            label = "CÁMARA",
            icon = {
                Icon(
                    imageVector = Icons.Outlined.PhotoCamera,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            onClick = { onEvent(CitizenHomeEvent.CaptureEvidenceClicked) }
        )

        Text(
            text = "Casos activos",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = { onEvent(CitizenHomeEvent.SearchQueryChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = "Buscar por nombre...",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp)
        )

        if (!hasLocationPermission && !uiState.isLoading) {
            LocationPermissionCard(onRequestLocationPermission = onRequestLocationPermission)
        }

        if (uiState.userLat != null && !uiState.isLoading) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Radio de búsqueda",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${uiState.radiusKm.roundToInt()} km",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Slider(
                    value = uiState.radiusKm,
                    onValueChange = { onEvent(CitizenHomeEvent.RadiusChanged(it)) },
                    valueRange = 5f..100f,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (uiState.filteredCases.isEmpty()) {
            Text(
                text = when {
                    uiState.isLoading -> "Cargando casos..."
                    uiState.searchQuery.isNotBlank() -> "Sin resultados para \"${uiState.searchQuery}\""
                    uiState.userLat != null -> "No se encontraron casos cercanos en un radio de ${uiState.radiusKm.roundToInt()} km."
                    else -> "Cargando casos..."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            uiState.filteredCases.forEach { case ->
                ActiveCaseCard(
                    case = case,
                    distanceKm = uiState.distanceTo(case),
                    onClick = { onEvent(CitizenHomeEvent.CaseCardClicked(case)) }
                )
            }
        }
    }

    if (uiState.selectedCase != null) {
        ModalBottomSheet(
            onDismissRequest = { onEvent(CitizenHomeEvent.DismissCaseModal) },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            CaseDetailSheet(
                case = uiState.selectedCase,
                onCargarReporte = { onCargarReporte(uiState.selectedCase.id) }
            )
        }
    }
}
@Composable
private fun CollaborationOptionCard(
    title: String,
    description: String,
    label: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit = {}
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 2.dp
        ),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            icon()

            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LocationPermissionCard(onRequestLocationPermission: () -> Unit) {
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
                text = "Permiso de ubicación",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Para ver casos cercanos y filtrar por distancia, Trobat necesita acceder a tu ubicación.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = onRequestLocationPermission,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(text = "Habilitar ubicación")
            }
        }
    }
}
