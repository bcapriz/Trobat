package com.trobat.ui.utils

import android.annotation.SuppressLint
import android.content.Context
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
    onResult: (Uri, Double, Double) -> Unit,
    onError: (String?) -> Unit
) {
    val photoFile = File(context.cacheDir, "evidence_${System.currentTimeMillis()}.jpg")
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                val photoUri = Uri.fromFile(photoFile)
                val fusedLocation = LocationServices.getFusedLocationProviderClient(context)
                val tokenSource = CancellationTokenSource()

                fusedLocation.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    tokenSource.token
                ).addOnSuccessListener { location ->
                    onResult(photoUri, location?.latitude ?: -34.6037, location?.longitude ?: -58.3816)
                }.addOnFailureListener {
                    onResult(photoUri, -34.6037, -58.3816)
                }
            }

            override fun onError(exception: ImageCaptureException) {
                onError(exception.message)
            }
        }
    )
}
