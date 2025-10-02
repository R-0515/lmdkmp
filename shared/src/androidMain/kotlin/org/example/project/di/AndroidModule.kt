package org.example.project.di

import com.google.android.gms.location.LocationServices
import org.example.project.NetworkMonitor
import org.example.project.SecureTokenStore
import org.example.project.SecureTokenStoreImpl
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
val SecureTokenAndroidModule = module {

    // Bind SecureTokenStore to SecureTokenStoreImpl
    single<SecureTokenStore> { SecureTokenStoreImpl(androidContext()) }

    // Bind NetworkMonitor
    single { NetworkMonitor(androidContext()) }
}