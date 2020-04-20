package com.storyous.delivery

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.storyous.delivery.api.Place
import com.storyous.delivery.common.DeliveryActivity
import com.storyous.delivery.common.DeliveryConfiguration
import com.storyous.delivery.common.PlaceInfo
import com.storyous.delivery.common.repositories.DeliveryRepository
import com.storyous.delivery.repositories.AuthRepository
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.ext.android.inject


@Suppress("TooManyFunctions")
class MainActivity : AppCompatActivity() {

    private val authRepository: AuthRepository by inject()
    private val deliveryRepository: DeliveryRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        authRepository.place.observe(this, Observer { place -> onPlaceResult(place) })
        authRepository.loginError.observe(this, Observer { error -> onLoginError(error) })

        webview.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                if (!authRepository.interceptLogin(url)) {
                    view.loadUrl(url)
                }
                return true
            }
        }

        authRepository.loginUrl?.let { url ->
            webview.loadUrl(url, mapOf("credentials" to "include"))
        }
    }

    private fun onPlaceResult(place: Place) {
        Toast.makeText(this, "Logged into place: $place", Toast.LENGTH_LONG).show()
        DeliveryConfiguration.deliveryRepository = deliveryRepository
        DeliveryConfiguration.placeInfo = PlaceInfo(place.placeId, place.merchantId, true)
        DeliveryActivity.launch(this)
    }

    private fun onLoginError(error: Error) {
        Toast.makeText(this, error.message, Toast.LENGTH_LONG).show()
        authRepository.loginUrl?.let { url ->
            webview.loadUrl(url, mapOf("credentials" to "include"))
        }
    }
}
