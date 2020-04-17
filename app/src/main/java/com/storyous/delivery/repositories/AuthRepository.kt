package com.storyous.delivery.repositories

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.storyous.commonutils.CoroutineProviderScope
import com.storyous.commonutils.provider
import com.storyous.delivery.BuildConfig
import com.storyous.delivery.api.ApiProvider
import com.storyous.delivery.api.LoginService
import com.storyous.delivery.api.Place
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.java.KoinJavaComponent.get
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import java.net.URLEncoder

open class AuthRepository(private val context: Context) :
    CoroutineScope by CoroutineProviderScope() {

    val place = MutableLiveData<Place>()
    val loginError = MutableLiveData<Error>()

    val loginUrl = String.format(
        BuildConfig.LOGIN_URL,
        URLEncoder.encode(BuildConfig.LOGIN_CLIENT_ID, "UTF-8"),
        URLEncoder.encode(BuildConfig.LOGIN_REDIRECT_URL, "UTF-8")
    )

    private val loginService = get(ApiProvider::class.java).get(LoginService::class)

    fun interceptLogin(url: String): Boolean {
        Timber.d("Intercepting URL: $url")
        return when (true) {
            Regex("error=([^&]+)").find(url)?.groups?.get(1)?.value?.let {
                Timber.e("Login failed with error: $it")
                loginError.value = Error(it)
                true
            } -> true
            Regex("code=([a-f0-9]+)").find(url)?.groups?.get(1)?.value?.let {
                exchangeCode(it)
                true
            } -> true
            else -> false
        }
    }

    private fun exchangeCode(code: String) = launch(provider.IO) {
        val token = try {
            loginService().authorize(
                "authorization_code",
                BuildConfig.LOGIN_CLIENT_ID,
                code
            ).string()
        } catch (ex: HttpException) {
            Timber.e(ex, "HTTP error on authorization exchange")
            ""
        } catch (ex: IOException) {
            Timber.e(ex, "IO error on authorization exchange")
            ""
        }
        if (token.isEmpty()) {
            withContext(provider.Main) {
                loginError.value = Error("No token, no fun")
            }
            return@launch
        }
        get(ApiProvider::class.java).onCredentialsChanged(token)
        val placesResponse = try {
            loginService().getPlaces()
        } catch (ex: HttpException) {
            Timber.e(ex, "HTTP error while getting places")
            null
        } catch (ex: IOException) {
            Timber.e(ex, "IO error while getting places")
            null
        }
        val newPlace = placesResponse?.data?.firstOrNull { it.integrations.isNotEmpty() }
        withContext(provider.Main) {
            if (newPlace == null) {
                loginError.value = Error("No place with integration, no fun")
            } else {
                place.value = newPlace
                println("FIRST PLACE: ${newPlace}")
            }
        }
    }
}
