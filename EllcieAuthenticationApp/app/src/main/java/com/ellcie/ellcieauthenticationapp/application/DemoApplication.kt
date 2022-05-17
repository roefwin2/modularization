package com.ellcie.ellcieauthenticationapp.application

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DemoApplication : Application() {
    val NOTIFICATION_HIGH_PRIORITY_CHANNEL_ID = "Notification_Ellcie_High_Importance"
    val NOTIFICATION_DEFAULT_PRIORITY_CHANNEL_ID = "Notification_Ellcie_Default_Importance"
    val NOTIFICATION_CHANNEL_NAME = "Ellcie Healthy"
}