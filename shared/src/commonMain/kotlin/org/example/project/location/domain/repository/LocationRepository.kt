package org.example.project.location.domain.repository

import org.example.project.location.domain.model.Coordinates

interface LocationRepository {
    suspend fun getLastLocation(): Coordinates?
    suspend fun getCurrentLocation(): Coordinates?
}