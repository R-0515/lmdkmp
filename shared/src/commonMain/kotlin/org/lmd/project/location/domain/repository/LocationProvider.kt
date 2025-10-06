package org.lmd.project.location.domain.repository

import org.lmd.project.location.domain.model.Coordinates

interface LocationProvider {
    suspend fun getLastKnownLocation(): Coordinates?
    suspend fun getCurrentLocation(): Coordinates?
}