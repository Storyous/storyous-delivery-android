package com.storyous.delivery

import androidx.lifecycle.Observer
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.multidex.MultiDexApplication
import com.facebook.stetho.Stetho
import com.storyous.delivery.common.DeliveryConfiguration
import com.storyous.delivery.common.repositories.DeliveryRepository
import com.storyous.delivery.repositories.AuthRepository
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber
import timber.log.Timber.DebugTree

class App : MultiDexApplication() {

    private val authRepository: AuthRepository by inject()

    private val deliveryRepository: DeliveryRepository by inject()

    override fun onCreate() {
        super.onCreate()

        initChannels(this)

        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
            Stetho.initializeWithDefaults(this);
        }

        startKoin {
            androidContext(this@App)
            modules(applicationModule)
        }

        deliveryRepository.apply {
            DeliveryConfiguration.deliveryRepository = this
            newDeliveriesToHandle.observe(
                ProcessLifecycleOwner.get(),
                Observer {
                    it.forEach { order ->
                        showNewOrderNotification(this@App, order)
                    }
                }
            )
            getDeliveryError().observe(
                ProcessLifecycleOwner.get(),
                Observer { authRepository.clear() }
            )
        }

        DeliveryConfiguration.onActivityToolbarCreate = { toolbar, _ ->
            toolbar.inflateMenu(R.menu.delivery_menu)
            toolbar.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.logout -> {
                        authRepository.clear()
                        LoginActivity.launch(this)
                        true
                    }
                    else -> false
                }
            }
        }
    }
}
