package org.example.project.map.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import org.example.project.location.data.provider.FusedLocationProviderImpl
import org.example.project.location.data.repository.LocationRepositoryImpl
import org.example.project.location.domain.model.Coordinates
import org.example.project.location.domain.usecase.GetDeviceLocationsUseCase

class MapLocationViewModel(app: Application) : AndroidViewModel(app) {
    private val provider = FusedLocationProviderImpl(app)
    private val repo = LocationRepositoryImpl(provider)
    private val getLocations = GetDeviceLocationsUseCase(repo)

    private val controller = MapLocationController(
        getDeviceLocations = getLocations,
        scope = viewModelScope
    )

    val location: StateFlow<Coordinates?> = controller.location

    fun loadMyLocation() = controller.loadMyLocation()
}
