package com.storyous.delivery.api

import retrofit2.HttpException
import retrofit2.http.GET
import java.io.IOException

interface ApiRouterService {

    @GET("merchants/me")
    @Throws(HttpException::class, IOException::class)
    suspend fun getMerchant(): Merchant
}
