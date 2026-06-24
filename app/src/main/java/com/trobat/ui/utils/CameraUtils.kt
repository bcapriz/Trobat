package com.trobat.ui.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location as AndroidLocation
import android.net.Uri
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import java.io.File

@SuppressLint("MissingPermission")
fun takePictureWithLocation(
    context: Context,
    imageCapture: ImageCapture,
    onResult: (Uri, Double?, Double?) -> Unit,
    onError: (String?) -> Unit
) {
    val fusedLocation = LocationServices.getFusedLocationProviderClient(context)
    val tokenSource = CancellationTokenSource()

    fusedLocation.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, tokenSource.token)
        .addOnSuccessListener { location ->
            capturePhotoWithExif(context, imageCapture, location?.latitude, location?.longitude, onResult, onError)
        }
        .addOnFailureListener {
            capturePhotoWithExif(context, imageCapture, null, null, onResult, onError)
        }
}

private fun capturePhotoWithExif(
    context: Context,
    imageCapture: ImageCapture,
    lat: Double?,
    lng: Double?,
    onResult: (Uri, Double?, Double?) -> Unit,
    onError: (String?) -> Unit
) {
    val photoFile = File(context.filesDir, "evidence_${System.currentTimeMillis()}.jpg")
    val metadata = ImageCapture.Metadata().apply {
        if (lat != null && lng != null) {
            location = AndroidLocation("gps").apply {
                latitude = lat
                longitude = lng
            }
        }
    }
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile)
        .setMetadata(metadata)
        .build()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                onResult(Uri.fromFile(photoFile), lat, lng)
            }
            override fun onError(exception: ImageCaptureException) {
                onError(exception.message)
            }
        }
    )
}
