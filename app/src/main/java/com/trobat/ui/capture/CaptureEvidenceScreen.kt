package com.trobat.ui.capture

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.core.app.ActivityCompat
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trobat.ui.utils.findActivity
import coil.compose.AsyncImage
import com.trobat.R
import com.trobat.ui.utils.takePictureWithLocation

@Composable
fun CaptureEvidenceScreen(
    onConfirmReport: () -> Unit = {},
    onCancel: () -> Unit = {},
    viewModel: CaptureEvidenceViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }
    var isLocationPermanentlyDenied by remember { mutableStateOf(false) }

    val permissionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasCameraPermission = permissions[Manifest.permission.CAMERA] == true
        val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        hasLocationPermission = locationGranted
        if (!locationGranted) {
            val activity = context.findActivity()
            isLocationPermanentlyDenied = activity != null &&
                    !ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            isLocationPermanentlyDenied = false
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val locationGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                hasCameraPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                hasLocationPermission = locationGranted
                if (locationGranted) isLocationPermanentlyDenied = false
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(hasCameraPermission, hasLocationPermission) {
        viewModel.onEvent(
            CaptureEvidenceEvent.PermissionsChanged(
                hasCameraPermission = hasCameraPermission,
                hasLocationPermission = hasLocationPermission
            )
        )
    }

    // Detecta denegación permanente al entrar a la pantalla sin esperar a que el usuario toque el botón.
    // Si el permiso no está otorgado, dispara el launcher silenciosamente: el sistema no muestra diálogo
    // cuando está bloqueado, pero el callback sí se ejecuta y actualiza isLocationPermanentlyDenied.
    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionsLauncher.launch(
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION)
            )
        }
    }

    LaunchedEffect(Unit) {
        if (viewModel.uiState.value.hasPhoto && CapturedEvidenceHolder.photoUri == null) {
            viewModel.onEvent(CaptureEvidenceEvent.RetakePhotoClicked)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                CaptureEvidenceEffect.NavigateToConfirmReport -> onConfirmReport()
            }
        }
    }

    val imageCapture = remember { ImageCapture.Builder().build() }

    val onTakePhoto: () -> Unit = {
        viewModel.onEvent(CaptureEvidenceEvent.TakePhotoClicked)
        takePictureWithLocation(
            context = context,
            imageCapture = imageCapture,
            onResult = { uri, lat, lng ->
                viewModel.onEvent(CaptureEvidenceEvent.PhotoCaptured(uri, lat, lng))
            },
            onError = { msg ->
                viewModel.onEvent(CaptureEvidenceEvent.CaptureError(msg))
            }
        )
    }

    CaptureEvidenceContent(
        uiState = uiState,
        imageCapture = imageCapture,
        isLocationPermanentlyDenied = isLocationPermanentlyDenied,
        onRequestPermissions = {
            permissionsLauncher.launch(
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION)
            )
        },
        onOpenSettings = {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
            context.startActivity(intent)
        },
        onTakePhoto = onTakePhoto,
        onCancel = onCancel,
        onEvent = viewModel::onEvent
    )
}

@Composable
private fun CaptureEvidenceContent(
    uiState: CaptureEvidenceUiState,
    imageCapture: ImageCapture,
    isLocationPermanentlyDenied: Boolean,
    onRequestPermissions: () -> Unit,
    onOpenSettings: () -> Unit,
    onTakePhoto: () -> Unit,
    onCancel: () -> Unit,
    onEvent: (CaptureEvidenceEvent) -> Unit
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
            text = stringResource(R.string.capture_title),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = stringResource(R.string.capture_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (!uiState.hasRequiredPermissions) {
            PermissionInfoCard(
                hasCameraPermission = uiState.hasCameraPermission,
                hasLocationPermission = uiState.hasLocationPermission,
                isLocationPermanentlyDenied = isLocationPermanentlyDenied,
                onRequestPermissions = onRequestPermissions,
                onOpenSettings = onOpenSettings
            )
        }

        CameraCard(uiState = uiState, imageCapture = imageCapture)

        uiState.errorMessage?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }

        val semanticsTomar = stringResource(R.string.capture_semantics_take)
        val semanticsUsar = stringResource(R.string.capture_semantics_use)
        val semanticsRehacer = stringResource(R.string.capture_semantics_retake)

        if (!uiState.hasPhoto) {
            Button(
                onClick = onTakePhoto,
                enabled = !uiState.isCapturing && uiState.hasRequiredPermissions,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .semantics { contentDescription = semanticsTomar }
            ) {
                Icon(imageVector = Icons.Outlined.PhotoCamera, contentDescription = null)
                Text(
                    text = stringResource(if (uiState.isCapturing) R.string.capture_taking_photo else R.string.capture_take_photo),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        } else {
            Button(
                onClick = { onEvent(CaptureEvidenceEvent.UseEvidenceClicked) },
                enabled = uiState.hasLocationData,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .semantics { contentDescription = semanticsUsar }
            ) {
                Icon(imageVector = Icons.Outlined.CheckCircle, contentDescription = null)
                Text(
                    text = stringResource(R.string.capture_use_evidence),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            OutlinedButton(
                onClick = { onEvent(CaptureEvidenceEvent.RetakePhotoClicked) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .semantics { contentDescription = semanticsRehacer }
            ) {
                Icon(imageVector = Icons.Outlined.Refresh, contentDescription = null)
                Text(
                    text = stringResource(R.string.capture_retake_photo),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(text = stringResource(R.string.action_cancel))
        }
    }
}

@Composable
private fun CameraCard(
    uiState: CaptureEvidenceUiState,
    imageCapture: ImageCapture
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }

    // Re-bind camera when permissions are granted or when returning to preview after a capture
    LaunchedEffect(uiState.hasRequiredPermissions, uiState.capturedPhotoUri) {
        if (!uiState.hasRequiredPermissions) return@LaunchedEffect
        if (uiState.capturedPhotoUri != null) return@LaunchedEffect
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.surfaceProvider = previewView.surfaceProvider
            }
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageCapture
            )
        }, ContextCompat.getMainExecutor(context))
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(430.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(28.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clip(RoundedCornerShape(28.dp)),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.capturedPhotoUri != null -> {
                    AsyncImage(
                        model = uiState.capturedPhotoUri,
                        contentDescription = stringResource(R.string.capture_photo_captured),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                uiState.isCapturing -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = stringResource(R.string.capture_capturing),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                uiState.hasRequiredPermissions -> {
                    AndroidView(
                        factory = { previewView },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                else -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PhotoCamera,
                            contentDescription = stringResource(R.string.capture_camera_preview),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stringResource(R.string.capture_camera_preview),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.capture_camera_enable),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionInfoCard(
    hasCameraPermission: Boolean,
    hasLocationPermission: Boolean,
    isLocationPermanentlyDenied: Boolean,
    onRequestPermissions: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val habilitada = stringResource(R.string.capture_permission_enabled)
    val pendiente = stringResource(R.string.capture_permission_pending)
    val bloqueada = stringResource(R.string.capture_permission_blocked)

    val locationLabel = when {
        hasLocationPermission -> habilitada
        isLocationPermanentlyDenied -> bloqueada
        else -> pendiente
    }

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
                text = stringResource(R.string.capture_permissions_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.capture_permissions_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.capture_camera_status, if (hasCameraPermission) habilitada else pendiente),
                style = MaterialTheme.typography.bodySmall,
                color = if (hasCameraPermission) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
            Text(
                text = stringResource(R.string.capture_location_status, locationLabel),
                style = MaterialTheme.typography.bodySmall,
                color = if (hasLocationPermission) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
            if (isLocationPermanentlyDenied) {
                Text(
                    text = stringResource(R.string.capture_location_permanently_denied_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Button(
                onClick = if (isLocationPermanentlyDenied) onOpenSettings else onRequestPermissions,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = stringResource(
                        if (isLocationPermanentlyDenied) R.string.capture_go_to_settings
                        else R.string.action_enable_permissions
                    )
                )
            }
        }
    }
}
