package org.example.project.map.vm

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.example.project.location.domain.model.Coordinates
import org.example.project.location.domain.usecase.GetDeviceLocationsUseCase

class MapLocationController(
    private val getDeviceLocations: GetDeviceLocationsUseCase,
    private val scope: CoroutineScope
) {
    private val _location = MutableStateFlow<Coordinates?>(null)
    val location: StateFlow<Coordinates?> = _location

    fun loadMyLocation() {
        scope.launch {
            val (last, current) = getDeviceLocations()
            _location.value = current ?: last
        }
    }
}
