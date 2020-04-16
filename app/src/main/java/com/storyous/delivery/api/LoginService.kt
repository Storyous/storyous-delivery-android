package com.storyous.delivery.api

import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import java.io.IOException

interface LoginService {

    @FormUrlEncoded
    @POST("auth/authorize")
    @Throws(HttpException::class, IOException::class)
    suspend fun authorize(
        @Field("grant_type") grantType: String,
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("code") code: String
    ): ResponseBody

    @GET("places")
    @Throws(HttpException::class, IOException::class)
    suspend fun getPlaces(): BaseDataResponse<List<Place>>
}
