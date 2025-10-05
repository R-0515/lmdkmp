package org.lmd.project.location.domain.repository

import org.lmd.project.location.domain.model.Coordinates

interface LocationRepository {
    suspend fun getLastLocation(): Coordinates?
    suspend fun getCurrentLocation(): Coordinates?
}