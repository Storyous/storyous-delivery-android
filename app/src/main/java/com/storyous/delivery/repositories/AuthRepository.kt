package com.storyous.delivery.repositories

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import com.storyous.commonutils.CoroutineProviderScope
import com.storyous.commonutils.onNonNull
import com.storyous.commonutils.provider
import com.storyous.delivery.BuildConfig
import com.storyous.delivery.LoginError
import com.storyous.delivery.LoginResult
import com.storyous.delivery.LoginSuccess
import com.storyous.delivery.api.ApiProvider
import com.storyous.delivery.api.LoginService
import com.storyous.delivery.api.Place
import com.storyous.delivery.common.DeliveryConfiguration
import com.storyous.delivery.common.PlaceInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.parameter.parametersOf
import org.koin.java.KoinJavaComponent.get
import retrofit2.HttpException
import timber.log.Timber
import java.net.URLEncoder

class AuthRepository : CoroutineScope by CoroutineProviderScope() {

    companion object {
        private const val SP_NAME = "com.storyous.delivery.auth"
        private const val SP_KEY_TOKEN = "token"
        private const val SP_KEY_PLACE_ID = "placeId"
        private const val SP_KEY_MERCHANT_ID = "merchantId"
    }

    private val sp = get(SharedPreferences::class.java) {
        parametersOf(SP_NAME, Context.MODE_PRIVATE)
    }
    val loginResult = MutableLiveData<LoginResult>()
    val loginUrl = String.format(
        BuildConfig.LOGIN_URL,
        URLEncoder.encode(BuildConfig.LOGIN_CLIENT_ID, "UTF-8"),
        URLEncoder.encode(BuildConfig.LOGIN_REDIRECT_URL, "UTF-8")
    )
    private val loginService = get(ApiProvider::class.java).get(LoginService::class)

    init {
        launch(provider.IO) {
            get(ApiProvider::class.java).onCredentialsChanged(sp.getString(SP_KEY_TOKEN, null))
            onNonNull(
                sp.getString(SP_KEY_PLACE_ID, null),
                sp.getString(SP_KEY_MERCHANT_ID, null)
            ) { placeId, merchantId ->
                withContext(provider.Main) {
                    handlePlace(PlaceInfo(placeId, merchantId, true))
                }
            }
        }
    }

    fun interceptLogin(url: String): Boolean {
        Timber.d("Intercepting URL: $url")
        return when (true) {
            Regex("auth-error\\?error=([^&]+)").find(url)?.groups?.get(1)?.value?.let {
                Timber.e("Login failed with error: $it")
                loginResult.value = LoginError(it)
                true
            } -> true
            Regex("code=([a-f0-9]+)").find(url)?.groups?.get(1)?.value?.let {
                exchangeCode(it)
                true
            } -> true
            else -> false
        }
    }

    private fun exchangeCode(code: String) = launch {
        val token = runCatching {
            withContext(provider.IO) {
                loginService().authorize(
                    "authorization_code",
                    BuildConfig.LOGIN_CLIENT_ID,
                    code
                ).string()
            }
        }.onFailure {
            Timber.e(it, "Error on authorization exchange")
            if (it is HttpException) {
                loginResult.value = LoginError(it.code())
            } else {
                loginResult.value = LoginError(LoginError.ERROR_NO_TOKEN)
            }
        }.onSuccess {
            get(ApiProvider::class.java).onCredentialsChanged(it)
        }.getOrElse {
            return@launch
        }

        val places = runCatching {
            withContext(provider.IO) {
                loginService().getPlaces()
            }
        }.onFailure {
            Timber.e(it, "HTTP error while getting places")
        }.getOrNull()?.data

        if ((places?.size ?: 0) > 1) {
            loginResult.value = LoginError(LoginError.ERROR_TOO_MANY_PLACES)
            return@launch
        }

        val newPlace = places?.firstOrNull { it.integrations.isNotEmpty() }
        if (newPlace == null) {
            loginResult.value = LoginError(LoginError.ERROR_NO_PLACE)
        } else {
            onLoginSuccess(newPlace, token)
        }

        if (loginResult.value is LoginError) {
            clear()
        }
    }

    private fun onLoginSuccess(place: Place, token: String) {
        sp.edit()
            .putString(SP_KEY_TOKEN, token)
            .putString(SP_KEY_PLACE_ID, place.placeId)
            .putString(SP_KEY_MERCHANT_ID, place.merchantId)
            .apply()

        handlePlace(PlaceInfo(place.placeId, place.merchantId, true))
    }

    private fun handlePlace(place: PlaceInfo) {
        DeliveryConfiguration.placeInfo = place
        loginResult.value = LoginSuccess(place)
        println("FIRST PLACE: $place")
    }

    fun clear() {
        get(ApiProvider::class.java).onCredentialsChanged(null)
        sp.edit().clear().apply()
        loginResult.value = null
    }
}
