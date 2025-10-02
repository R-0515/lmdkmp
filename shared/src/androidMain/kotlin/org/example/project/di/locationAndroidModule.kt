package org.example.project.di

import com.google.android.gms.location.LocationServices
import org.example.project.location.data.repository.AndroidLocationProvider
import org.example.project.location.domain.repository.LocationProvider
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module


val locationAndroidModule = module {
    // FusedLocationProviderClient (needs Context)
    single { LocationServices.getFusedLocationProviderClient(androidContext()) }

    // Provider API (callback/suspend style)
    single<LocationProvider> { AndroidLocationProvider(get()) }
}

