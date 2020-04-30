package com.storyous.delivery

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.storyous.delivery.api.Place
import com.storyous.delivery.repositories.AuthRepository
import org.koin.java.KoinJavaComponent.inject

class LoginViewModel : ViewModel() {

    private val authRepository: AuthRepository by inject(AuthRepository::class.java)
    val loginResult: LiveData<LoginResult?> = authRepository.loginResult
    val loginUrl = authRepository.loginUrl

    fun interceptLogin(url: String) = authRepository.interceptLogin(url)

    fun errorConsumed() {
        authRepository.loginResult.value = null
    }

    fun placeChoiceDone(place: Place) {
        loginResult.value?.run {
            if (this is LoginPlaceChoice) {
                authRepository.placeChoiceDone(merchant.merchantId, place.placeId, token)
            }
        }
    }
}
