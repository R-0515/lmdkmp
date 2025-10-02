package org.example.project.location.domain.model

import com.google.android.gms.maps.model.LatLng

fun Coordinates.toLatLng(): LatLng = LatLng(latitude, longitude)
