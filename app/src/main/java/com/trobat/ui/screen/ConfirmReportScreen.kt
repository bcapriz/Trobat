package com.trobat.ui.screen

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.trobat.ui.viewmodel.ConfirmReportEffect
import com.trobat.ui.viewmodel.ConfirmReportEvent
import com.trobat.ui.viewmodel.ConfirmReportUiState
import com.trobat.ui.viewmodel.ConfirmReportViewModel
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                ConfirmReportEffect.NavigateToHeatMap -> {
                    onSendReport()
                }

                ConfirmReportEffect.NavigateBackToCamera -> {
                    onRetakePhoto()
                }
            }
        }
    }

    ConfirmReportContent(
        uiState = uiState,
        onCancel = onCancel,
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
            text = "Confirmar reporte",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Revisá la evidencia y agregá información útil antes de enviarla.",
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
                        contentDescription = "Foto capturada",
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
                            contentDescription = "Foto capturada",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Foto capturada",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        ActiveCaseDropdown(
            activeCases = uiState.activeCases,
            selectedCase = uiState.selectedCase,
            showError = uiState.showCaseError,
            onCaseSelected = { caseId ->
                onEvent(ConfirmReportEvent.CaseSelected(caseId))
            }
        )
        OutlinedTextField(
            value = uiState.requiredDescription,
            onValueChange = { newValue ->
                onEvent(ConfirmReportEvent.RequiredDescriptionChanged(newValue))
            },
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = "Campo obligatorio para describir lo visto"
                },
            label = {
                Text(text = "¿Qué viste? *")
            },
            placeholder = {
                Text(text = "Ej: Vi a una persona similar cerca de la plaza.")
            },
            supportingText = {
                if (uiState.showRequiredDescriptionError) {
                    Text(text = "Este campo es obligatorio")
                } else {
                    Text(text = "Campo obligatorio")
                }
            },
            isError = uiState.showRequiredDescriptionError,
            minLines = 3,
            maxLines = 4,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences
            )
        )

        OutlinedTextField(
            value = uiState.optionalDetails,
            onValueChange = { newValue ->
                onEvent(ConfirmReportEvent.OptionalDetailsChanged(newValue))
            },
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = "Campo opcional para agregar datos adicionales"
                },
            label = {
                Text(text = "Datos adicionales")
            },
            placeholder = {
                Text(text = "Ej: Vestimenta, dirección hacia donde caminaba, compañía, etc.")
            },
            supportingText = {
                Text(text = "Campo opcional")
            },
            minLines = 2,
            maxLines = 3,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences
            )
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
                    contentDescription = "Ubicación",
                    tint = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Ubicación de la foto",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )

                if (uiState.latitude != null && uiState.longitude != null) {
                    Text(
                        text = "Lat: ${"%.5f".format(uiState.latitude)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Long: ${"%.5f".format(uiState.longitude)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "Ubicación no disponible",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Identificarme en el reporte",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (uiState.isIdentified)
                        "El reporte se enviará con tus datos de contacto."
                    else
                        "El reporte se enviará de forma anónima.",
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
            onClick = {
                onEvent(ConfirmReportEvent.SendReportClicked)
            },
            enabled = uiState.canSendReport,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.CheckCircle,
                contentDescription = null
            )

            Text(
                text = if (uiState.isSending) {
                    "Enviando..."
                } else {
                    "Enviar reporte"
                },
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        OutlinedButton(
            onClick = { onEvent(ConfirmReportEvent.RetakePhotoClicked) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(text = "Rehacer foto")
        }

        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(text = "Cancelar")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActiveCaseDropdown(
    activeCases: List<MissingPersonCase>,
    selectedCase: MissingPersonCase?,
    showError: Boolean,
    onCaseSelected: (String) -> Unit
) {
    var expanded by remember {
        mutableStateOf(false)
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            expanded = !expanded
        }
    ) {
        OutlinedTextField(
            value = selectedCase?.let { "${it.fullName}, ${it.age} años" } ?: "",
            onValueChange = {},
            readOnly = true,
            label = {
                Text(text = "¿A qué caso corresponde este reporte? *")
            },
            placeholder = {
                Text(text = "Seleccioná un caso activo")
            },
            supportingText = {
                if (showError) {
                    Text(text = "Debés seleccionar un caso activo")
                } else {
                    Text(text = "Campo obligatorio")
                }
            },
            isError = showError,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {
            activeCases.forEach { activeCase ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(
                                text = "${activeCase.fullName}, ${activeCase.age} años",
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
                        expanded = false
                    }
                )
            }
        }
    }
}