package com.storyous.delivery

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.storyous.delivery.common.DeliveryActivity
import com.storyous.delivery.common.api.model.DeliveryOrder
import kotlin.random.Random

private const val NOTIF_CHANNEL_ID = "storyous_delivery_id"

fun initChannels(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
        return
    }

    val channel = NotificationChannel(
        NOTIF_CHANNEL_ID,
        "Storyous deliveries channel",
        NotificationManager.IMPORTANCE_DEFAULT
    )
    channel.description = "Channel containing received orders from Storyous delivery."
    context.notificationManager.createNotificationChannel(channel)
}

fun showNewOrderNotification(context: Context, order: DeliveryOrder) {

    val intent = DeliveryActivity.createLaunchIntent(context, order.orderId).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }

    val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

    val builder = NotificationCompat.Builder(context, NOTIF_CHANNEL_ID)
        .setOngoing(true)
        .setSmallIcon(R.drawable.ic_notifications_black_24dp)
        .setContentTitle(context.getString(R.string.app_name))
        .setContentText(context.getString(R.string.notification_new_order, order.customer.name))
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)

    with(context.notificationManager) {
        // notificationId is a unique int for each notification that you must define
        notify(Random(0).nextInt(), builder.build())
    }
}

val Context.notificationManager
    get() = NotificationManagerCompat.from(this)
