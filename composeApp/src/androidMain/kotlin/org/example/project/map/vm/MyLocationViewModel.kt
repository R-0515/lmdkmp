package org.example.project.map.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.project.location.data.provider.FusedLocationProviderImpl
import org.example.project.location.data.repository.LocationRepositoryImpl
import org.example.project.location.domain.model.Coordinates
import org.example.project.location.domain.usecase.GetDeviceLocationsUseCase

class MyLocationViewModel(app: Application) : AndroidViewModel(app) {

    private val _location = MutableStateFlow<Coordinates?>(null)
    val location: StateFlow<Coordinates?> = _location.asStateFlow()

    fun loadMyLocation() {
        viewModelScope.launch {
            val provider = FusedLocationProviderImpl(getApplication())
            val repo = LocationRepositoryImpl(provider)
            val getLocations = GetDeviceLocationsUseCase(repo)
            val (last, current) = getLocations()
            _location.value = current ?: last
        }
    }
}
