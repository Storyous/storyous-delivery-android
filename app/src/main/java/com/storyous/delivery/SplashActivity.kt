package com.storyous.delivery

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.storyous.commonutils.AlarmUtils
import com.storyous.delivery.common.DeliveryActivity
import com.storyous.delivery.common.DownloadDeliveryReceiver
import com.storyous.delivery.repositories.AuthRepository
import org.koin.android.ext.android.inject

class SplashActivity : AppCompatActivity() {

    private val authRepository: AuthRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        when (authRepository.loginResult.value) {
            is LoginSuccess -> {
                DeliveryActivity.launch(this)
                AlarmUtils.keepWakeUp(this)
                AlarmUtils.setRepeatingAlarm(
                    this,
                    DownloadDeliveryReceiver::class,
                    0,
                    AlarmUtils.MIN_INTERVAL
                )
            }
            else -> LoginActivity.launch(this)
        }
    }
}
