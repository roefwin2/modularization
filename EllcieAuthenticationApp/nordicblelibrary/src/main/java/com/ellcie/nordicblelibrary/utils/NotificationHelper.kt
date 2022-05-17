package com.ellcie_healthy.ble_library.ble.utils

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import com.ellcie_healthy.ble_library.R
import no.nordicsemi.android.ble.livedata.state.ConnectionState

const val NOTIFICATION_HIGH_PRIORITY_CHANNEL_ID = "Notification_Ellcie_High_Importance"
const val NOTIFICATION_DEFAULT_PRIORITY_CHANNEL_ID = "Notification_Ellcie_Default_Importance"
private const val NOTIFICATION_LIGHT = Color.RED
private const val TIME_LIGHT_ON = 3000
private const val TIME_LIGHT_OFF = 3000


fun createNotificationBuilder(
    context: Context,
    title: String,
    text: String,
    clazz: Class<*>?,
    priority: Int
): NotificationCompat.Builder {
    val iconEllcie = BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher)
    var notificationChannelId = NOTIFICATION_DEFAULT_PRIORITY_CHANNEL_ID
    if (priority >= NotificationCompat.PRIORITY_HIGH && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        notificationChannelId = NOTIFICATION_HIGH_PRIORITY_CHANNEL_ID
    }
    val builder: NotificationCompat.Builder =
        NotificationCompat.Builder(context, notificationChannelId)
            .setSmallIcon(R.drawable.ic_logo_ellcie_healthy)
            .setLargeIcon(iconEllcie) //@FIX cannot find icon on oneplus 3
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(text)
            ) //exendable text, to show the text in his integrality
            .setContentTitle(title)
            .setContentText(text)
            .setLights(NOTIFICATION_LIGHT, TIME_LIGHT_ON, TIME_LIGHT_OFF) //set light
            .setAutoCancel(true)
            .setPriority(priority)
    builder.setVibrate(longArrayOf(0L)) // Passing null here silently fails
    if (clazz != null) { //check here to avoid crash
        // Creates an explicit intent for an Activity in your app
        val notificationIntent = Intent(context, clazz)
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        builder.setContentIntent(pendingIntent)
    }
    return builder
}

 fun createNotification(
    clazz: Class<*>?,
    state: ConnectionState.State,
    deviceName: String,
    context: Context
): Notification {
    var message: String? = ""
    when (state) {
        ConnectionState.State.CONNECTING -> if (deviceName.isEmpty()) {
            message = context.getString(R.string.device_status_connecting_no_name)
        } else {
            message = context.getString(R.string.device_status_connecting, deviceName)
        }
        ConnectionState.State.DISCONNECTED -> if (deviceName == null || deviceName.isEmpty()) {
            message = context.getString(R.string.device_status_disconnected_no_name)
        } else {
            message = context.getString(R.string.device_status_disconnected, deviceName)
        }
        ConnectionState.State.DISCONNECTING -> if (deviceName == null || deviceName.isEmpty()) {
            message = context.getString(R.string.device_status_disconnecting_no_name)
        } else {
            message = context.getString(R.string.device_status_disconnecting, deviceName)
        }
        ConnectionState.State.INITIALIZING -> message =
            context.getString(R.string.device_status_initializing, deviceName)
        ConnectionState.State.READY -> message =
            context.getString(R.string.device_status_connected, deviceName)
    }
    val iconEllcie = BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher)
    val builder: NotificationCompat.Builder =
        NotificationCompat.Builder(context, NOTIFICATION_DEFAULT_PRIORITY_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_logo_ellcie_healthy)
            .setLargeIcon(iconEllcie) //@FIX cannot find icon on oneplus 3
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(message)
            ) //exendable text, to show the text in his integrality
            .setContentTitle("Ellcie Service")
            .setContentText(message)
            .setLights(NOTIFICATION_LIGHT, TIME_LIGHT_ON, TIME_LIGHT_OFF) //set light
            .setAutoCancel(true)
    if (clazz != null) {
        val notificationIntent = Intent(context, clazz)
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        builder.setContentIntent(pendingIntent) // Set the class (homeactivty) that will be called when the user click on the notification and only if the clazz object is not null
    }

    return builder.build()
}