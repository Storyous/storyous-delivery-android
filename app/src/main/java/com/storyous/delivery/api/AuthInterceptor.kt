package com.storyous.storyouspay.api

import android.os.Build
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Create Authorization header and other analytics headers in all requests.
 *
 */
class AuthInterceptor : Interceptor {
    companion object {
        const val HEADER_REQUEST_ID = "X-Request-ID"
        const val HEADER_AUTH = "Authorization"
    }

    internal val customHeaders = HashMap<String, String>()
    internal var authHeaderProvider: AuthHeaderProvider? = null

    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().newBuilder()

        builder.header(HEADER_REQUEST_ID, System.nanoTime().toString())
        builder.header("AndroidVersion", Build.VERSION.RELEASE)

        HashMap(customHeaders).forEach { builder.header(it.key, it.value) }

        if (chain.request().header(HEADER_AUTH) == null) {
            authHeaderProvider?.getAuth()?.let {
                builder.header(HEADER_AUTH, it)
            }
        }

        return chain.proceed(builder.build())
    }

}
