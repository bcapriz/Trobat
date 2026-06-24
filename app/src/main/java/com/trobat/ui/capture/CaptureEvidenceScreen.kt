package com.trobat.ui.capture

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.trobat.ui.capture.CapturedEvidenceHolder
import com.trobat.ui.utils.takePictureWithLocation
import com.trobat.ui.capture.CaptureEvidenceEffect
import com.trobat.ui.capture.CaptureEvidenceEvent
import com.trobat.ui.capture.CaptureEvidenceUiState
import com.trobat.ui.capture.CaptureEvidenceViewModel

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

    val permissionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasCameraPermission = permissions[Manifest.permission.CAMERA] == true
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
    }

    LaunchedEffect(hasCameraPermission, hasLocationPermission) {
        viewModel.onEvent(
            CaptureEvidenceEvent.PermissionsChanged(
                hasCameraPermission = hasCameraPermission,
                hasLocationPermission = hasLocationPermission
            )
        )
    }

    LaunchedEffect(Unit) {
        // If state was restored with a photo but the evidence was already sent (holder cleared),
        // reset so the user starts a fresh capture instead of seeing the old photo
        if (viewModel.uiState.value.hasPhoto && CapturedEvidenceHolder.photoUri == null) {
            viewModel.onEvent(CaptureEvidenceEvent.RetakePhotoClicked)
        }

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
        onRequestPermissions = {
            permissionsLauncher.launch(
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION)
            )
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
    onRequestPermissions: () -> Unit,
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
            text = "Capturar evidencia",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Tomá una foto desde la app. Luego podrás agregar descripción y asociarla a un caso activo.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (!uiState.hasRequiredPermissions) {
            PermissionInfoCard(
                hasCameraPermission = uiState.hasCameraPermission,
                hasLocationPermission = uiState.hasLocationPermission,
                onRequestPermissions = onRequestPermissions
            )
        }

        CameraCard(
            uiState = uiState,
            imageCapture = imageCapture
        )

        uiState.errorMessage?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }

        if (!uiState.hasPhoto) {
            Button(
                onClick = onTakePhoto,
                enabled = !uiState.isCapturing && uiState.hasRequiredPermissions,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .semantics { contentDescription = "Tomar foto para reportar evidencia" }
            ) {
                Icon(imageVector = Icons.Outlined.PhotoCamera, contentDescription = null)
                Text(
                    text = if (uiState.isCapturing) "Tomando foto..." else "Tomar foto",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        } else {
            Button(
                onClick = { onEvent(CaptureEvidenceEvent.UseEvidenceClicked) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .semantics { contentDescription = "Usar esta evidencia para continuar" }
            ) {
                Icon(imageVector = Icons.Outlined.CheckCircle, contentDescription = null)
                Text(text = "Usar esta evidencia", modifier = Modifier.padding(start = 8.dp))
            }

            OutlinedButton(
                onClick = { onEvent(CaptureEvidenceEvent.RetakePhotoClicked) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .semantics { contentDescription = "Rehacer foto" }
            ) {
                Icon(imageVector = Icons.Outlined.Refresh, contentDescription = null)
                Text(text = "Rehacer foto", modifier = Modifier.padding(start = 8.dp))
            }
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
                        contentDescription = "Foto capturada",
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
                            text = "Capturando evidencia...",
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
                            contentDescription = "Vista previa de cámara",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Vista previa de cámara",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Habilitá los permisos para continuar.",
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
    onRequestPermissions: () -> Unit
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
                text = "Permisos necesarios",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Para reportar evidencia, Trobat necesita acceder a la cámara y a tu ubicación.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Cámara: ${if (hasCameraPermission) "habilitada" else "pendiente"}",
                style = MaterialTheme.typography.bodySmall,
                color = if (hasCameraPermission) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
            Text(
                text = "Ubicación: ${if (hasLocationPermission) "habilitada" else "pendiente"}",
                style = MaterialTheme.typography.bodySmall,
                color = if (hasLocationPermission) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
            Button(
                onClick = onRequestPermissions,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(text = "Habilitar permisos")
            }
        }
    }
}
