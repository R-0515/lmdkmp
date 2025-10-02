package org.example.project.location.domain.usecase

import org.example.project.location.domain.model.Coordinates
import org.example.project.location.domain.repository.LocationRepository

class GetDeviceLocationsUseCase(
    private val locationRepo: LocationRepository,
) {
    suspend operator fun invoke(): Pair<Coordinates?, Coordinates?> {
        val last = locationRepo.getLastLocation()
        val current = locationRepo.getCurrentLocation()
        return last to current
    }
}