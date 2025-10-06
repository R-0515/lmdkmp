package org.example.project.myPool.domian.mapper

import android.location.Location
import org.example.project.socket.Coordinates

fun Location?.toCoordinates(): Coordinates? =
    Coordinates(lat = this?.latitude, lng = this?.longitude)