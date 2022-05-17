package com.ellcie_healthy.common.utils.com.ellcie_healthy.common.firebase

import android.content.Context


interface IAnalyticsHelper {
    fun sendEvent(context: Context?, event: String?, ignore: Boolean)
}