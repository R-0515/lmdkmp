package org.lmd.project

import android.app.Application
import org.lmd.project.di.androidAuthModule
import org.lmd.project.di.authCommonModule
import org.lmd.project.di.SecureTokenAndroidModule
import org.lmd.project.di.locationAndroidModule
import org.lmd.project.di.locationCommonModule
import org.lmd.project.di.networkModule
import org.lmd.project.di.socketModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin
import org.lmd.project.di.deliveryAndroidModule
import org.lmd.project.di.orderHistoryAndroidModule

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
                    networkModule,
                    socketModule,
                    SecureTokenAndroidModule,
                    deliveryAndroidModule,
                    orderHistoryAndroidModule,
                ),
            )
        }
    }
}
