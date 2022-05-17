package com.ellcie_healthy.common.utils.com.ellcie_healthy.common

import java.util.*


/**
 * Created by Yann on 31/10/2018.
 */
class SessionId private constructor() {
    var sessionId = ""
    private var mUsedByService = false
    private var mUsedByActivity = false
    private fun onDestroy() {
        //Destroy the instance when the app is destroyed to avoid memory leaks
        sInstance = null
    }

    fun usedByActivity() {
        mUsedByActivity = true
    }

    fun activityDestroyed() {
        mUsedByActivity = false
        if (!mUsedByService) {
            onDestroy()
        }
    }

    companion object {
        private var sInstance: SessionId? = null
        @JvmStatic
        val instance: SessionId?
            get() {
                if (sInstance == null) {
                    sInstance = SessionId()
                }
                return sInstance
            }
    }

    init {
        sessionId = UUID.randomUUID().toString()
    }
}