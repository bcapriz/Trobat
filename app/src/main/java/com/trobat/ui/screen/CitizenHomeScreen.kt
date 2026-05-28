package com.trobat.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.PersonSearch
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trobat.data.model.CitizenReport
import com.trobat.data.model.MissingPersonCase
import com.trobat.ui.viewmodel.CitizenHomeEffect
import com.trobat.ui.viewmodel.CitizenHomeEvent
import com.trobat.ui.viewmodel.CitizenHomeUiState
import com.trobat.ui.viewmodel.CitizenHomeViewModel

@Composable
fun CitizenHomeScreen(
    onOpenMap: () -> Unit = {},
    onCaptureEvidence: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: CitizenHomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                CitizenHomeEffect.NavigateToMap -> {
                    onOpenMap()
                }

                CitizenHomeEffect.NavigateToCamera -> {
                    onCaptureEvidence()
                }
            }
        }
    }

    CitizenHomeContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        modifier = modifier
    )
}

@Composable
private fun CitizenHomeContent(
    uiState: CitizenHomeUiState,
    onEvent: (CitizenHomeEvent) -> Unit,
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

        HomeStatsRow(
            totalCases = uiState.totalCases,
            totalReports = uiState.totalReports,
            unreadNotifications = uiState.unreadNotifications
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
            }
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
            }
        )

        if (uiState.activeCases.isNotEmpty()) {
            Text(
                text = "Casos activos",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )

            uiState.activeCases.forEach { case ->
                ActiveCaseCard(case = case)
            }
        }

        uiState.latestReport?.let { report ->
            Text(
                text = "Último reporte",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )

            LatestReportCard(report = report)
        }

        uiState.errorMessage?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }

        Button(
            onClick = {
                onEvent(CitizenHomeEvent.OpenMapClicked)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .semantics {
                    contentDescription = "Ver el mapa de reportes ciudadanos"
                }
        ) {
            Icon(
                imageVector = Icons.Outlined.Map,
                contentDescription = null
            )

            Text(
                text = "Ver el mapa",
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        OutlinedButton(
            onClick = {
                onEvent(CitizenHomeEvent.CaptureEvidenceClicked)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .semantics {
                    contentDescription = "Reportar evidencia usando la cámara"
                }
        ) {
            Icon(
                imageVector = Icons.Outlined.PhotoCamera,
                contentDescription = null
            )

            Text(
                text = "Reportar evidencia",
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        OutlinedButton(
            onClick = {
                onEvent(CitizenHomeEvent.RefreshClicked)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Refresh,
                contentDescription = null
            )

            Text(
                text = "Actualizar",
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}
@Composable
private fun HomeStatsRow(
    totalCases: Int,
    totalReports: Int,
    unreadNotifications: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HomeStatCard(
            title = "Casos activos",
            value = totalCases.toString(),
            modifier = Modifier.weight(1f)
        )

        HomeStatCard(
            title = "Reportes",
            value = totalReports.toString(),
            modifier = Modifier.weight(1f)
        )

        HomeStatCard(
            title = "Alertas",
            value = unreadNotifications.toString(),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun HomeStatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CollaborationOptionCard(
    title: String,
    description: String,
    label: String,
    icon: @Composable () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
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
private fun ActiveCaseCard(
    case: MissingPersonCase
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.PersonSearch,
                    contentDescription = "Persona buscada",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "${case.fullName}, ${case.age} años",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = case.physicalDescription,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 2.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Place,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Visto por última vez: ${case.lastSeenLocation}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = case.lastSeenDate,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 18.dp)
            )
        }
    }
}

@Composable
private fun LatestReportCard(
    report: CitizenReport
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Place,
                contentDescription = "Ubicación del último reporte",
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                text = report.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = report.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "${report.address} • ${report.createdAt}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}