package com.storyous.delivery

import android.content.Context
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Handler
import android.os.Looper

class RingtonePlayer(context: Context, private val interval: Long = 15000L) {

    private val handler = Handler(Looper.getMainLooper())

    private val playRingtone = Runnable {
        ringtonePlayer.start()
    }

    private val ringtonePlayer: MediaPlayer by lazy {
        with(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)) {
            MediaPlayer.create(context.applicationContext, this)
        }.apply {
            setOnCompletionListener {
                handler.postDelayed(playRingtone, interval)
            }
        }
    }

    fun play() = handler.postDelayed(playRingtone, interval)

    fun pause() = handler.removeCallbacks(playRingtone)
}
