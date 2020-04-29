package com.storyous.delivery

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.storyous.commonutils.AlarmUtils
import com.storyous.delivery.api.Place
import com.storyous.delivery.common.DeliveryActivity
import com.storyous.delivery.common.DownloadDeliveryReceiver
import com.storyous.delivery.common.PlaceInfo
import kotlinx.android.synthetic.main.activity_login.*
import timber.log.Timber

@Suppress("TooManyFunctions")
class LoginActivity : AppCompatActivity() {

    private val viewModel: LoginViewModel by viewModels()

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

        viewModel.loginResult.observe(this, Observer { result -> onLoginResult(result) })

        webview.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                if (!viewModel.interceptLogin(url)) {
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
            is LoginPlaceChoice -> onPlaceChoice(result.places, result.token)
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
        val messageResId = when {
            error.errorCode == LoginError.ERROR_TOO_MANY_PLACES -> R.string.error_not_supported_multiple_places
            error.isRecoverable() -> R.string.error_recoverable
            else -> R.string.error_fatal
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.error_header, error.errorCode))
            .setMessage(getString(messageResId, error.errorCode))
            .setPositiveButton(R.string.understand) { dialog, _ ->
                viewModel.errorConsumed()
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
        viewModel.loginUrl.let { url ->
            webview.loadUrl(
                url, mapOf(
                    "credentials" to "include",
                    "Accept-Language" to LocaleUtil().getAcceptedLanguageHeaderValue()
                )
            )
        }
    }

    private fun onPlaceChoice(places: List<Place>, token: String) {
        val adapter = ArrayAdapter(
            this,
            R.layout.choose_place_singlechoice,
            places.map { it.name }.toTypedArray()
        )
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.choose_place))
            .setSingleChoiceItems(adapter, -1) { dialog, which ->
                viewModel.placeChoiceDone(places[which], token)
                dialog.dismiss()
            }
            .setOnCancelListener { reload() }
            .setNegativeButton(R.string.cancel) { _, _ -> reload() }
            .show()
    }
}
