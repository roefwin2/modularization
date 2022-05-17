package com.ellcie_healthy.common.utils.com.ellcie_healthy.common.firebase

import android.util.Log
import com.ellcie_healthy.common.utils.com.ellcie_healthy.common.SessionId
import java.util.*


/**
 * Created by Julien on 11/05/2021
 */
class GlassesLogFb(// in millis
        val time: Long, private val mCode: String, private val mReason: String, private val mData: String, private val mSerialNumber: String) {

    fun convertForFirebaseDatabase(userUid: String): Dictionary<String, Any> {
        val map: Dictionary<String, Any> = Hashtable()
        map.put(TIMESTAMP_KEY, time)
        map.put(CODE_KEY, mCode)
        map.put(REASON_KEY, mReason)
        map.put(DATA_KEY, mData)
        map.put(UID_KEY, userUid)
        map.put(GLASSES_KEY, mSerialNumber)
        map.put(SID_KEY, SessionId.instance?.sessionId)
        Log.d(TAG, "convertForFirebaseDatabase: map: " + toString())
        return map
    }

    override fun toString(): String {
        return "GlassesLogFb{" +
                "mTime =" + time +
                ", mCode='" + mCode + '\'' +
                ", mReason='" + mReason + '\'' +
                ", mData='" + mData + '\'' +
                ", mSerialNumber='" + mSerialNumber + '\'' +
                '}'
    }

    companion object {
        private const val TIMESTAMP_KEY = "ts" // in millis.
        private const val CODE_KEY = "code" // code example 0x01 representing SHUTDOWN
        private const val REASON_KEY = "reason" // code example 0x01 representing SHUTDOWN
        private const val DATA_KEY = "data" // containing the data of a log. For example "2.0.0" (the firmware version)
        private const val UID_KEY = "uid" // uid user
        private const val GLASSES_KEY = "glasses" // glasses
        private const val SID_KEY = "sid" // UUID unique.
        private const val TAG = "GlassesLogFb"
    }
}
