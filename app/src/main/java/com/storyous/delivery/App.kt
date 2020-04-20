package com.storyous.delivery

import androidx.lifecycle.Observer
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.multidex.MultiDexApplication
import com.storyous.delivery.common.repositories.DeliveryRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent
import timber.log.Timber
import timber.log.Timber.DebugTree

class App : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()

        initChannels(this)

        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }

        startKoin {
            androidContext(this@App)
            modules(applicationModule)
        }

        KoinJavaComponent.get(DeliveryRepository::class.java).apply {
            newDeliveriesToHandle.observe(
                ProcessLifecycleOwner.get(),
                Observer {
                    it.forEach { order ->
                        showNewOrderNotification(this@App, order)
                    }
                }
            )
        }
    }

}
