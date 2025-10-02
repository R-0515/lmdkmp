package org.example.project.location.domain.repository

import org.example.project.location.domain.model.Coordinates

interface LocationProvider {
    suspend fun getLastKnownLocation(): Coordinates?
    suspend fun getCurrentLocation(): Coordinates?
}