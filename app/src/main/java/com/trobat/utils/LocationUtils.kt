package com.trobat.utils

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

fun AndroidViewModel.fetchCurrentLocation(onLocation: (Pair<Double, Double>?) -> Unit) {
    val app = getApplication<Application>()
    val granted = ContextCompat.checkSelfPermission(app, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(app, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
    if (!granted) {
        onLocation(null)
        return
    }

    val client = LocationServices.getFusedLocationProviderClient(app)
    val tokenSource = CancellationTokenSource()
    client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, tokenSource.token)
        .addOnSuccessListener { location ->
            onLocation(if (location != null) Pair(location.latitude, location.longitude) else null)
        }
        .addOnFailureListener { onLocation(null) }
}
