package com.storyous.delivery.api

import com.storyous.delivery.BuildConfig
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.http.*
import java.io.IOException

interface LoginService {

    @FormUrlEncoded
    @POST("auth/authorize")
    @Headers("Origin: ${BuildConfig.LOGIN_ORIGIN_HEADER}")
    @Throws(HttpException::class, IOException::class)
    suspend fun authorize(
        @Field("grant_type") grantType: String,
        @Field("client_id") clientId: String,
        @Field("code") code: String
    ): ResponseBody

    @GET("places")
    @Throws(HttpException::class, IOException::class)
    suspend fun getPlaces(): BaseDataResponse<List<Place>>
}
