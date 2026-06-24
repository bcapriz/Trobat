package com.trobat.ui.home

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
import androidx.compose.material.icons.outlined.Description
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
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trobat.R
import com.trobat.data.model.MissingPersonCase
import com.trobat.ui.components.ActiveCaseCard
import com.trobat.ui.components.CaseDetailSheet
import com.trobat.ui.components.RadiusSlider
import kotlin.math.roundToInt

@Composable
fun CitizenHomeScreen(
    onOpenMap: () -> Unit = {},
    onCaptureEvidence: () -> Unit = {},
    onResumeDraft: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: CitizenHomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onEvent(CitizenHomeEvent.ScreenResumed)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

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
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                CitizenHomeEffect.NavigateToMap -> onOpenMap()
                CitizenHomeEffect.NavigateToCamera -> onCaptureEvidence()
                CitizenHomeEffect.NavigateToConfirmReport -> onResumeDraft()
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
            viewModel.onEvent(CitizenHomeEvent.CaseSelectedForReport(caseId))
        },
        modifier = modifier,
        onResumeDraft = onResumeDraft
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
    onResumeDraft: () -> Unit = {},
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
            text = stringResource(R.string.home_app_label),
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
            text = stringResource(R.string.home_subtitulo),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (uiState.hasPendingDraft) {
            PendingDraftCard(onResumeDraft = { onEvent(CitizenHomeEvent.ResumeDraftClicked) })
        }

        CollaborationOptionCard(
            title = stringResource(R.string.home_mapa_titulo),
            description = stringResource(R.string.home_mapa_desc),
            label = stringResource(R.string.home_mapa_label),
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
            title = stringResource(R.string.home_camara_titulo),
            description = stringResource(R.string.home_camara_desc),
            label = stringResource(R.string.home_camara_label),
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
            text = stringResource(R.string.home_casos_activos),
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
                    text = stringResource(R.string.home_buscar_placeholder),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            leadingIcon = {
                Icon(imageVector = Icons.Outlined.Search, contentDescription = null)
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp)
        )

        if (!hasLocationPermission && !uiState.isLoading) {
            LocationPermissionCard(onRequestLocationPermission = onRequestLocationPermission)
        }

        if (uiState.userLat != null && !uiState.isLoading) {
            RadiusSlider(
                radiusKm = uiState.radiusKm,
                onRadiusChanged = { onEvent(CitizenHomeEvent.RadiusChanged(it)) }
            )
        }

        if (uiState.isLoading) {
            Text(
                text = stringResource(R.string.home_cargando),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else if (uiState.isSearching) {
            Text(
                text = stringResource(R.string.home_buscando),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else if (uiState.filteredCases.isEmpty()) {
            Text(
                text = when {
                    uiState.searchQuery.isNotBlank() -> stringResource(R.string.sin_resultados_busqueda, uiState.searchQuery)
                    uiState.userLat != null -> stringResource(R.string.home_sin_casos_cercanos, uiState.radiusKm.roundToInt())
                    else -> stringResource(R.string.home_sin_casos)
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
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
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
private fun PendingDraftCard(onResumeDraft: () -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onResumeDraft),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(22.dp)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Description,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.home_draft_titulo),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.home_draft_subtitulo),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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
                text = stringResource(R.string.home_ubicacion_titulo),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.home_ubicacion_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = onRequestLocationPermission,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(text = stringResource(R.string.home_habilitar_ubicacion))
            }
        }
    }
}
