package com.storyous.delivery

import com.storyous.delivery.api.ApiProvider
import com.storyous.delivery.repositories.AuthRepository
import com.storyous.storyouspay.api.AuthInterceptor
import org.koin.dsl.module

val applicationModule = module {
    single { AuthRepository(get()) }
    single { AuthInterceptor() }
    single { ApiProvider(get(), get()) }
}
