package org.lmd.project.location.data.repository

import org.lmd.project.location.domain.model.Coordinates
import org.lmd.project.location.domain.repository.LocationProvider
import org.lmd.project.location.domain.repository.LocationRepository

class LocationRepositoryImpl(
    private val provider: LocationProvider
) : LocationRepository {
    override suspend fun getLastLocation(): Coordinates? =
        provider.getLastKnownLocation()

    override suspend fun getCurrentLocation(): Coordinates? =
        provider.getCurrentLocation()
}