package com.storyous.delivery.api

import com.google.gson.Gson
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


class ApiBuilder(
    baseUrl: String,
    authInterceptor: Interceptor?,
    gson: Gson = Gson(),
    cache: Cache? = null,
    converterFactory: Converter.Factory = GsonConverterFactory.create(gson)
) {
    companion object {
        const val CONNECT_TIMEOUT = 25L
        const val WRITE_TIMEOUT = 25L
        const val READ_TIMEOUT = 80L
    }

    private val retrofit: Retrofit

    init {
        val builder = OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)

        if (cache != null) {
            builder.cache(cache)
        }
        authInterceptor?.let { builder.addInterceptor(it) }
        builder.addInterceptor(
            HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        )

        retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(builder.build())
            .addConverterFactory(converterFactory)
            .build()
    }

    fun <T> build(cls: Class<T>): T {
        return retrofit.create(cls)
    }

    fun <T> buildError(cls: Class<T>): Converter<ResponseBody, T> {
        return retrofit.responseBodyConverter(cls, arrayOfNulls<Annotation>(0))
    }
}
