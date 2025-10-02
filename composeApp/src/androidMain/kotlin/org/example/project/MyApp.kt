package org.example.project

import android.app.Application
import org.example.project.di.generalPoolAndroidModule
import org.example.project.di.generalPoolCommonModule
import org.example.project.di.locationAndroidModule
import org.example.project.di.locationCommonModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MyApp)
            modules(
                listOf(
                    locationCommonModule,
                    locationAndroidModule,
                    generalPoolCommonModule,
                    generalPoolAndroidModule
                ),
            )
        }
    }
}
