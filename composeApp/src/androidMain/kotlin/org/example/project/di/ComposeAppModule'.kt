package org.example.project.di

import org.example.project.auth.viewmodel.LoginViewModel
import org.example.project.auth.domain.usecase.LoginUseCase
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val composeAppModule = module {
    viewModel { LoginViewModel(loginUseCase = get<LoginUseCase>()) }
}