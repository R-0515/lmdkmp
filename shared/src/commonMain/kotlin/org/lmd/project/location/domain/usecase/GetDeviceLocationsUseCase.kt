package org.lmd.project.location.domain.usecase

import org.lmd.project.location.domain.model.Coordinates
import org.lmd.project.location.domain.repository.LocationRepository

class GetDeviceLocationsUseCase(
    private val locationRepo: LocationRepository,
) {
    suspend operator fun invoke(): Pair<Coordinates?, Coordinates?> {
        val last = locationRepo.getLastLocation()
        val current = locationRepo.getCurrentLocation()
        return last to current
    }
}