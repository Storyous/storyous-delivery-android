package com.storyous.delivery

import com.storyous.delivery.api.ApiProvider
import com.storyous.delivery.common.api.DeliveryService
import com.storyous.delivery.common.db.DeliveryDatabase
import com.storyous.delivery.common.repositories.DeliveryRepository
import com.storyous.delivery.repositories.AuthRepository
import com.storyous.storyouspay.api.AuthInterceptor
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val applicationModule = module {
    single(createdAtStart = true) { AuthRepository() }
    single { AuthInterceptor() }
    single { ApiProvider(get(), get()) }
    single {
        DeliveryRepository(
            (get() as ApiProvider).get(DeliveryService::class),
            DeliveryDatabase(get()).deliveryDao()
        )
    }
    single { (name: String, mode: Int) -> androidApplication().getSharedPreferences(name, mode) }
}
