package com.trobat.ui.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import android.widget.Toast
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.trobat.R
import com.trobat.data.model.MissingPersonCase
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun ConfirmReportScreen(
    onSendReport: () -> Unit = {},
    onRetakePhoto: () -> Unit = {},
    onCancel: () -> Unit = {},
    viewModel: ConfirmReportViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val sinConexionMsg = stringResource(R.string.confirm_sin_conexion)

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                ConfirmReportEffect.NavigateToHeatMap -> onSendReport()
                ConfirmReportEffect.NavigateBackToCamera -> onRetakePhoto()
                ConfirmReportEffect.ReportSavedLocally -> {
                    Toast.makeText(context, sinConexionMsg, Toast.LENGTH_LONG).show()
                    onSendReport()
                }
                ConfirmReportEffect.NavigateToCases -> onCancel()
            }
        }
    }

    ConfirmReportContent(
        uiState = uiState,
        onCancel = { viewModel.onEvent(ConfirmReportEvent.CancelClicked) },
        onEvent = viewModel::onEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConfirmReportContent(
    uiState: ConfirmReportUiState,
    onCancel: () -> Unit,
    onEvent: (ConfirmReportEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp)
            .padding(top = 40.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = stringResource(R.string.confirm_titulo),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = stringResource(R.string.confirm_subtitulo),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.photoUri != null) {
                    AsyncImage(
                        model = uiState.photoUri,
                        contentDescription = stringResource(R.string.confirm_foto_capturada),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PhotoCamera,
                            contentDescription = stringResource(R.string.confirm_foto_capturada),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stringResource(R.string.confirm_foto_capturada),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        ActiveCaseDropdown(
            displayedCases = uiState.displayedCases,
            selectedCaseLabel = uiState.selectedCaseLabel,
            searchQuery = uiState.caseSearchQuery,
            isSearching = uiState.isCaseSearching,
            showError = uiState.showCaseError,
            onSearchQueryChanged = { onEvent(ConfirmReportEvent.CaseSearchQueryChanged(it)) },
            onCaseSelected = { caseId -> onEvent(ConfirmReportEvent.CaseSelected(caseId)) },
            onDismiss = { onEvent(ConfirmReportEvent.CaseSearchQueryChanged("")) }
        )

        val semanticsFotoDesc = stringResource(R.string.confirm_semantics_foto)
        OutlinedTextField(
            value = uiState.requiredDescription,
            onValueChange = { newValue ->
                onEvent(ConfirmReportEvent.RequiredDescriptionChanged(newValue))
            },
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = semanticsFotoDesc },
            label = { Text(text = stringResource(R.string.confirm_que_viste_label)) },
            placeholder = { Text(text = stringResource(R.string.confirm_que_viste_placeholder)) },
            supportingText = {
                if (uiState.showRequiredDescriptionError) {
                    Text(text = stringResource(R.string.confirm_campo_obligatorio_error))
                } else {
                    Text(text = stringResource(R.string.confirm_campo_obligatorio))
                }
            },
            isError = uiState.showRequiredDescriptionError,
            minLines = 3,
            maxLines = 4,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )

        val semanticsAdicionalesDesc = stringResource(R.string.confirm_semantics_adicionales)
        OutlinedTextField(
            value = uiState.optionalDetails,
            onValueChange = { newValue ->
                onEvent(ConfirmReportEvent.OptionalDetailsChanged(newValue))
            },
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = semanticsAdicionalesDesc },
            label = { Text(text = stringResource(R.string.confirm_adicionales_label)) },
            placeholder = { Text(text = stringResource(R.string.confirm_adicionales_placeholder)) },
            supportingText = { Text(text = stringResource(R.string.confirm_campo_opcional)) },
            minLines = 2,
            maxLines = 3,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )

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
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = stringResource(R.string.confirm_ubicacion_icon_desc),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.confirm_ubicacion_titulo),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = uiState.locationLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.confirm_identificarme),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(
                        if (uiState.isIdentified) R.string.confirm_con_datos else R.string.confirm_anonimo
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = uiState.isIdentified,
                onCheckedChange = { onEvent(ConfirmReportEvent.IdentificationToggled(it)) }
            )
        }

        Button(
            onClick = { onEvent(ConfirmReportEvent.SendReportClicked) },
            enabled = uiState.canSendReport,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Icon(imageVector = Icons.Outlined.CheckCircle, contentDescription = null)
            Text(
                text = stringResource(if (uiState.isSending) R.string.confirm_enviando else R.string.confirm_enviar),
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        OutlinedButton(
            onClick = { onEvent(ConfirmReportEvent.RetakePhotoClicked) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(text = stringResource(R.string.confirm_rehacer))
        }

        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(text = stringResource(R.string.accion_cancelar))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActiveCaseDropdown(
    displayedCases: List<MissingPersonCase>,
    selectedCaseLabel: String?,
    searchQuery: String,
    isSearching: Boolean,
    showError: Boolean,
    onSearchQueryChanged: (String) -> Unit,
    onCaseSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { open ->
            expanded = open
            if (!open) onDismiss()
        }
    ) {
        OutlinedTextField(
            value = if (expanded) searchQuery else selectedCaseLabel ?: "",
            onValueChange = { if (expanded) onSearchQueryChanged(it) },
            label = { Text(text = stringResource(R.string.confirm_dropdown_label)) },
            placeholder = {
                Text(
                    text = if (expanded) stringResource(R.string.confirm_dropdown_buscar_placeholder)
                    else stringResource(R.string.confirm_dropdown_placeholder)
                )
            },
            supportingText = {
                if (showError) Text(text = stringResource(R.string.confirm_dropdown_error))
                else Text(text = stringResource(R.string.confirm_campo_obligatorio))
            },
            isError = showError,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
                onDismiss()
            }
        ) {
            when {
                isSearching -> {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(R.string.confirm_dropdown_buscando),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        onClick = {}
                    )
                }
                displayedCases.isEmpty() -> {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(R.string.confirm_dropdown_sin_resultados, searchQuery),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        onClick = {}
                    )
                }
                else -> {
                    displayedCases.forEach { activeCase ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        text = stringResource(R.string.case_nombre_edad, activeCase.fullName, activeCase.age),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${activeCase.lastSeenLocation} • ${activeCase.area}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            onClick = {
                                onCaseSelected(activeCase.id)
                                onDismiss()
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}
