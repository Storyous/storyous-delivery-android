package com.storyous.delivery.api

import android.content.Context
import com.google.gson.Gson
import com.storyous.delivery.BuildConfig
import com.storyous.delivery.common.api.model.DeliveryErrorResponse
import com.storyous.storyouspay.api.AuthHeaderProvider
import com.storyous.storyouspay.api.AuthInterceptor
import com.storyous.storyouspay.api.DeliveryService
import com.storyous.storyouspay.api.PosErrorConverterWrapper
import okhttp3.Cache
import kotlin.reflect.KClass

/**
 * Responsible for create and update retrofit apis
 */
@Suppress("TooManyFunctions")
class ApiProvider(context: Context, private val authInterceptor: AuthInterceptor = AuthInterceptor()) {

    @Suppress("MagicNumber")
    private val cacheSize: Long = 10 * 1024 * 1024 // 10 MB
    private var cache: Cache? = null

    private val services = mutableMapOf<KClass<out Any>, Any>()

    lateinit var errorConverter: PosErrorConverterWrapper

    companion object {
        private const val LOCALHOST = "http://localhost/"
    }

    init {
        cache = Cache(context.cacheDir, cacheSize)
        createLoginService(BuildConfig.LOGIN_API_URL)
        createDeliveryService(BuildConfig.DELIVERY_API_URL)
    }

    private fun createLoginService(url: String) {
        ApiBuilder(url, authInterceptor)
            .also {
                setService(LoginService::class, it.build(LoginService::class.java))
                errorConverter = PosErrorConverterWrapper(it.buildError(ErrorResponse::class.java))
            }
    }

    private fun createDeliveryService(url: String) {
        ApiBuilder(url, authInterceptor, Gson(), cache)
            .also {
                setService(DeliveryService::class, it.build(DeliveryService::class.java))
                DeliveryErrorConverterWrapper.INSTANCE = DeliveryErrorConverterWrapper(
                    it.buildError(DeliveryErrorResponse::class.java)
                )
            }
    }

    /**
     * Set headers to all requests.
     */
    fun onCredentialsChanged(
        authToken: String?
    ) {
        authInterceptor.authHeaderProvider = authToken?.let {
            object : AuthHeaderProvider {
                override fun getAuth(): String {
                    return "Bearer $it"
                }
            }
        }
    }

    /**
     * Set header which will be send in all requests.
     */
    fun setRequestHeader(name: String, value: String?): ApiProvider {
        if (value == null) {
            authInterceptor.customHeaders.remove(name)
        } else {
            authInterceptor.customHeaders[name] = value
        }
        return this
    }

    fun <T : Any> get(clazz: KClass<T>): () -> T {
        return { services[clazz] as T }
    }

    private fun <T : Any> setService(clazz: KClass<T>, service: T) {
        services[clazz] = service
    }
}
