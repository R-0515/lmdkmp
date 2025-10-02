package org.example.project.di

import com.google.android.gms.location.LocationServices
import org.example.project.location.data.repository.AndroidLocationProvider
import org.example.project.location.domain.repository.LocationProvider
import org.example.project.myPool.ui.viewmodel.ActiveAgentsViewModel
import org.example.project.myPool.ui.viewmodel.UpdateOrderStatusViewModel
import org.example.project.myPool.ui.viewmodel.myOrder.MyOrdersViewModel
import org.example.project.myPool.ui.viewmodel.myPool.MyPoolViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val locationAndroidModule = module {
    // FusedLocationProviderClient (needs Context)
    single { LocationServices.getFusedLocationProviderClient(androidContext()) }

    // Provider API (callback/suspend style)
    single<LocationProvider> { AndroidLocationProvider(get()) }
}

val androidViewModelModule = module {
    viewModel { MyOrdersViewModel(get(), get(), get()) }
    viewModel { MyPoolViewModel(get(), get()) }
    viewModel { UpdateOrderStatusViewModel(get(), get()) }
    viewModel { ActiveAgentsViewModel(get()) }
}