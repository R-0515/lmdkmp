package org.example.project.location.data.repository

import org.example.project.location.domain.model.Coordinates
import org.example.project.location.domain.repository.LocationProvider
import org.example.project.location.domain.repository.LocationRepository

class LocationRepositoryImpl(
    private val provider: LocationProvider
) : LocationRepository {
    override suspend fun getLastLocation(): Coordinates? =
        provider.getLastKnownLocation()

    override suspend fun getCurrentLocation(): Coordinates? =
        provider.getCurrentLocation()
}