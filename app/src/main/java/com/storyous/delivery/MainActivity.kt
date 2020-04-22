package com.storyous.delivery

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.storyous.commonutils.AlarmUtils
import com.storyous.delivery.common.DeliveryActivity
import com.storyous.delivery.common.DeliveryConfiguration
import com.storyous.delivery.common.DownloadDeliveryReceiver
import com.storyous.delivery.common.PlaceInfo
import com.storyous.delivery.common.repositories.DeliveryRepository
import com.storyous.delivery.repositories.AuthRepository
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.ext.android.inject
import timber.log.Timber


@Suppress("TooManyFunctions")
class MainActivity : AppCompatActivity() {

    private val authRepository: AuthRepository by inject()
    private val deliveryRepository: DeliveryRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        authRepository.placeLive.observe(this, Observer { place -> onPlaceResult(place) })
        authRepository.loginError.observe(this, Observer { error -> onLoginError(error) })

        webview.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                if (!authRepository.interceptLogin(url)) {
                    view.loadUrl(url)
                }
                return true
            }
        }

        authRepository.loginUrl.let { url ->
            webview.loadUrl(url, mapOf("credentials" to "include"))
        }
    }

    private fun onPlaceResult(place: PlaceInfo) {
        Timber.i("Logged into place: $place")
        DeliveryActivity.launch(this)
        AlarmUtils.keepWakeUp(this)
        AlarmUtils.setRepeatingAlarm(
            this,
            DownloadDeliveryReceiver::class,
            0,
            AlarmUtils.MIN_INTERVAL
        )
    }

    private fun onLoginError(error: Error) {
        Toast.makeText(this, error.message, Toast.LENGTH_LONG).show()
        authRepository.loginUrl.let { url ->
            webview.loadUrl(url, mapOf("credentials" to "include"))
        }
    }
}
