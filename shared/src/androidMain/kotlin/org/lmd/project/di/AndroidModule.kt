package org.lmd.project.di

import com.google.android.gms.location.LocationServices
import org.example.project.generalPool.vm.GeneralPoolViewModel
import org.lmd.project.NetworkMonitor
import org.lmd.project.SecureTokenStore
import org.lmd.project.SecureTokenStoreImpl
import org.lmd.project.location.data.repository.AndroidLocationProvider
import org.lmd.project.location.domain.repository.LocationProvider
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import org.lmd.project.UserStore
import org.lmd.project.utils.AndroidUserStore

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

    // Bind UserStore
    single<UserStore> { AndroidUserStore(androidContext()) }
}

val generalPoolAndroidModule = module {
    viewModel {
        GeneralPoolViewModel(
            ordersRealtime = get(),
            computeDistances = get(),
            getDeviceLocations = get(),
            loadOrdersUseCase = get(),
        )
    }
}
