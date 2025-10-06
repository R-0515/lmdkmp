package org.lmd.project.location.data.repository

import android.annotation.SuppressLint
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine
import org.lmd.project.location.domain.model.Coordinates
import org.lmd.project.location.domain.repository.LocationProvider
import kotlin.coroutines.resume

/**
 The LocationProviderImpl class is platform-specific because it uses FusedLocationProviderClient (Google Play Services).
So should put it inside  shared/androidMain source set, not in commonMain.
 **/
class AndroidLocationProvider(
    private val fusedClient: FusedLocationProviderClient
) : LocationProvider {

    @SuppressLint("MissingPermission")
    override suspend fun getLastKnownLocation(): Coordinates? =
        suspendCancellableCoroutine { cont ->
            fusedClient.lastLocation
                .addOnSuccessListener { loc ->
                    cont.resume(loc?.let { Coordinates(it.latitude, it.longitude) })
                }
                .addOnFailureListener { cont.resume(null) }
        }

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): Coordinates? =
        suspendCancellableCoroutine { cont ->
            fusedClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                .addOnSuccessListener { loc ->
                    cont.resume(loc?.let { Coordinates(it.latitude, it.longitude) })
                }
                .addOnFailureListener { cont.resume(null) }
        }
}