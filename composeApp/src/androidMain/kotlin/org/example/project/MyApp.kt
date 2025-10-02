package org.example.project

import android.app.Application
import org.example.project.di.androidAuthModule
import org.example.project.di.authCommonModule
import org.example.project.di.composeAppModule
import org.example.project.di.SecureTokenAndroidModule
import org.example.project.di.generalPoolAndroidModule
import org.example.project.di.generalPoolCommonModule
import org.example.project.di.locationAndroidModule
import org.example.project.di.locationCommonModule
import org.example.project.di.networkModule
import org.example.project.di.socketModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MyApp)
            modules(
                listOf(
                    locationCommonModule,
                    locationAndroidModule,
                    authCommonModule,
                    androidAuthModule,
                    composeAppModule,
                    networkModule,
                    socketModule,
                    SecureTokenAndroidModule,
                    generalPoolAndroidModule,
                    generalPoolCommonModule
                ),
            )
        }
    }
}
