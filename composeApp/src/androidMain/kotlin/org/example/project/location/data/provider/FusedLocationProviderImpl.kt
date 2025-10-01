package org.example.project.location.data.provider

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await
import org.example.project.location.domain.model.Coordinates
import org.example.project.location.domain.repository.LocationProvider

class FusedLocationProviderImpl(
    private val context: Context
) : LocationProvider {

    private val fused by lazy { LocationServices.getFusedLocationProviderClient(context) }

    @SuppressLint("MissingPermission") // ensure caller requested permission first
    override suspend fun getLastKnownLocation(): Coordinates? {
        val loc = fused.lastLocation.await() ?: return null
        return Coordinates(loc.latitude, loc.longitude)
    }

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): Coordinates? {
        val token = CancellationTokenSource()
        val loc = fused.getCurrentLocation(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            token.token
        ).await() ?: return null
        return Coordinates(loc.latitude, loc.longitude)
    }
}
