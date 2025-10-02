package org.example.project.location.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Coordinates(
    val latitude: Double,
    val longitude: Double,
)

fun Coordinates.isValid(): Boolean =
    latitude.isFinite() &&
            longitude.isFinite() &&
            !(latitude == 0.0 && longitude == 0.0)