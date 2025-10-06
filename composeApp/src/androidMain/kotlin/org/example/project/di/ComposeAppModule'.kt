package org.example.project.di

import org.example.project.auth.viewmodel.LoginViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val composeAppModule = module {
    viewModel { LoginViewModel(get()) }
}

/*
val myPoolModule = module {
    // ViewModel registration
    viewModel {
        MyOrdersViewModel(
            getMyOrders = get<GetMyOrdersUseCase>(),
            computeDistancesUseCase = get<ComputeDistancesUseCase>(),
        )
    }
}
*/
