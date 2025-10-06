package org.lmd.project.location.domain.model

import com.google.android.gms.maps.model.LatLng

internal fun Coordinates.toLatLng(): LatLng = LatLng(latitude, longitude)
