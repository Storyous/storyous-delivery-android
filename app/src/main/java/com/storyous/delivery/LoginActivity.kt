package com.storyous.delivery

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.storyous.commonutils.AlarmUtils
import com.storyous.delivery.common.DeliveryActivity
import com.storyous.delivery.common.DownloadDeliveryReceiver
import com.storyous.delivery.common.PlaceInfo
import com.storyous.delivery.repositories.AuthRepository
import kotlinx.android.synthetic.main.activity_login.*
import org.koin.android.ext.android.inject
import timber.log.Timber


@Suppress("TooManyFunctions")
class LoginActivity : AppCompatActivity() {

    private val authRepository: AuthRepository by inject()

    companion object {

        fun launch(context: Context) {
            context.startActivity(Intent(context, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        authRepository.loginResult.observe(this, Observer { result -> onLoginResult(result) })

        webview.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                if (!authRepository.interceptLogin(url)) {
                    view.loadUrl(url)
                }
                return true
            }
        }
    }

    override fun onStart() {
        super.onStart()
        reload()
    }

    private fun onLoginResult(result: LoginResult?) {
        when (result) {
            is LoginSuccess -> onPlaceResult(result.placeInfo)
            is LoginError -> onLoginError(result)
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

    private fun onLoginError(error: LoginError) {
        val message = getString(
            if (error.isRecoverable()) {
                R.string.error_recoverable
            } else {
                R.string.error_fatal
            }, error.errorCode
        )

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.error_header)
            .setMessage(message)
            .setPositiveButton(R.string.accept_order) { dialog, _ ->
                if (error.isRecoverable()) {
                    reload()
                } else {
                    dialog.dismiss()
                    finish()
                }
            }
            .show()
    }

    private fun reload() {
        webview.clearCache(false)
        webview.clearFormData()
        authRepository.loginUrl.let { url ->
            webview.loadUrl(url, mapOf("credentials" to "include"))
        }
    }
}
