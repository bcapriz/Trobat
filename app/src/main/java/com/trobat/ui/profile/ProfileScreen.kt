package com.trobat.ui.profile

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AssignmentInd
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trobat.R

private enum class ProfileDialog(@StringRes val titleRes: Int, @StringRes val bodyRes: Int) {
    TERMS(R.string.profile_terms_title, R.string.profile_terms_body),
    PRIVACY(R.string.profile_privacy_title, R.string.profile_privacy_body),
    HELP(R.string.profile_help_title, R.string.profile_help_body)
}

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var activeDialog by remember { mutableStateOf<ProfileDialog?>(null) }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                ProfileEffect.NavigateToLogin -> onLogout()
            }
        }
    }

    activeDialog?.let { dialog ->
        AlertDialog(
            onDismissRequest = { activeDialog = null },
            title = {
                Text(
                    text = stringResource(dialog.titleRes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = stringResource(dialog.bodyRes),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = { activeDialog = null }) {
                    Text(stringResource(R.string.accion_entendido))
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp)
            .padding(top = 48.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        ProfileHeader(name = uiState.name, email = uiState.email)

        SectionLabel(stringResource(R.string.profile_section_mi_cuenta))
        AccountInfoCard(
            name = uiState.name,
            email = uiState.email,
            nationalId = uiState.nationalId,
            phone = uiState.phone
        )

        SectionLabel(stringResource(R.string.profile_section_configuracion))
        SettingsCard {
            SettingsToggleRow(
                icon = Icons.Outlined.DarkMode,
                label = stringResource(R.string.profile_dark_mode),
                checked = uiState.darkModeEnabled,
                onToggle = { viewModel.onEvent(ProfileEvent.DarkModeToggled(it)) }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            SettingsInfoRow(
                icon = Icons.Outlined.Info,
                label = stringResource(R.string.profile_version_label),
                value = stringResource(R.string.profile_version_value)
            )
        }

        SectionLabel(stringResource(R.string.profile_section_notificaciones))
        SettingsCard {
            SettingsToggleRow(
                icon = Icons.Outlined.Notifications,
                label = stringResource(R.string.profile_alertas),
                checked = uiState.notificationsEnabled,
                onToggle = { viewModel.onEvent(ProfileEvent.NotificationsToggled(it)) }
            )
        }

        SectionLabel(stringResource(R.string.profile_section_informacion))
        SettingsCard {
            SettingsNavRow(
                icon = Icons.Outlined.Gavel,
                label = stringResource(R.string.profile_terminos),
                onClick = { activeDialog = ProfileDialog.TERMS }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            SettingsNavRow(
                icon = Icons.Outlined.Security,
                label = stringResource(R.string.profile_privacidad),
                onClick = { activeDialog = ProfileDialog.PRIVACY }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            SettingsNavRow(
                icon = Icons.AutoMirrored.Outlined.HelpOutline,
                label = stringResource(R.string.profile_ayuda),
                onClick = { activeDialog = ProfileDialog.HELP }
            )
        }

        Button(
            onClick = { viewModel.onEvent(ProfileEvent.LogoutClicked) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.Logout,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.profile_cerrar_sesion),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ProfileHeader(name: String, email: String) {
    val initials = name.split(" ")
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .take(2)
        .joinToString("")
        .ifEmpty { "?" }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = name.ifEmpty { stringResource(R.string.profile_nombre_fallback) },
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (email.isNotEmpty()) {
            Text(
                text = email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}

@Composable
private fun AccountInfoCard(
    name: String,
    email: String,
    nationalId: String,
    phone: String
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(4.dp)) {
            AccountField(icon = Icons.Outlined.Person, label = stringResource(R.string.profile_field_nombre), value = name)
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            AccountField(icon = Icons.Outlined.Email, label = stringResource(R.string.profile_field_email), value = email)
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            AccountField(icon = Icons.Outlined.AssignmentInd, label = stringResource(R.string.profile_field_dni), value = nationalId)
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            AccountField(icon = Icons.Outlined.Phone, label = stringResource(R.string.profile_field_telefono), value = phone)
        }
    }
}

@Composable
private fun AccountField(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value.ifEmpty { "—" },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(4.dp), content = content)
    }
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    label: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                uncheckedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

@Composable
private fun SettingsNavRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun SettingsInfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
