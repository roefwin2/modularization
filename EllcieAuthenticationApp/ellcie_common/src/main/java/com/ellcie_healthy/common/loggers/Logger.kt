package com.ellcie_healthy.common.utils.com.ellcie_healthy.common.loggers

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.os.Process
import android.util.Log
import com.ellcie_healthy.common.utils.ServiceMode
import com.ellcie_healthy.common.utils.com.ellcie_healthy.common.callbacks.EllcieLog
import com.ellcie_healthy.common.utils.com.ellcie_healthy.common.firebase.IFirebaseDb
import com.google.firebase.crashlytics.FirebaseCrashlytics
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean

/** Created by Julien on 06/05/2021.
 *
 * Logger is a substitutes of Log class.
 * It store logs (warning, error, info) in a List
 * This list can be flushed when time is elapsed.
 *
 * This "time" can be defined by calling setPeriodToFlush
 */
class Logger : Runnable {

    override fun run() {
        // Moves the current Thread into the background
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
    }

    /**
     * Represents a log message
     */
    class LogItem {
        //@Clean we don't store locally the log anymore
        @com.google.gson.annotations.Expose
        private var mServiceMode : ServiceMode

        var title // log id
                : String?
            private set

        /**
         * Returns additionnal message given at runtime.
         *
         * @return
         */
        @com.google.gson.annotations.Expose
        var trace // message log, defined at static time.
                : String
            private set

        @com.google.gson.annotations.Expose
        private var mNum // numeric message
                : Number?

        @com.google.gson.annotations.Expose
        var time // in millis
                : Long
            private set




        /**
         * @param serviceMode
         * @param title
         * @param trace
         * @param trace
         * @param timeMillis
         */
        constructor(serviceMode: ServiceMode, title: String, trace: String, timeMillis: Long) {
            this.mServiceMode = serviceMode
            this.title = title
            this.trace = trace
            mNum = null
            time = timeMillis
        }

        /**
         *
         * @param title
         * @param numericValue
         * @param timeMillis
         */
        constructor(serviceMode: ServiceMode, title: String,
                    numericValue: Number, timeMillis: Long) {
            this.mServiceMode = serviceMode
            this.title = title
            mNum = numericValue
            time = timeMillis
            trace = ""
        }

        override fun toString(): String {
            val stringBuilder = StringBuilder()
            stringBuilder.append("{session id: ")
            stringBuilder.append("{id: ")
            stringBuilder.append(title)
            stringBuilder.append("trace: ")
            stringBuilder.append(trace)
            if (mNum != null) {
                stringBuilder.append("num: ")
                stringBuilder.append(mNum)
            }
            stringBuilder.append("time: ")
            stringBuilder.append(time)
            stringBuilder.append("product")
            stringBuilder.append(mServiceMode)
            stringBuilder.append("}")
            return stringBuilder.toString()
        }

        fun convertForFirebaseDatabase(userUid: String): Dictionary<String, Any>? {
            if (title == null) {
                return null
            }
            val map: Dictionary<String, Any> = Hashtable()
          //  map.put(LogItem.Companion.SID_KEY, SessionId.getInstance().getSessionId())
            map.put(CODE_KEY, title)
            map.put(TIMESTAMP_KEY, time)
            map.put(MESSAGE_KEY, trace)
            if (mNum != null) {
                map.put(NUMERIC_KEY, mNum)
            }
     //       map.put(LogItem.Companion.GLASSES_KEY, Glasses.getInstance().getSerialNumber())
            map.put(UID_KEY, userUid)
            map.put(PRODUCT_KEY, mServiceMode.toString())
            return map
        }

        companion object {
            private const val PRODUCT_KEY = "product" // product key, Serenity or Driver.
            private const val CODE_KEY = "code" // log id.
            private const val TIMESTAMP_KEY = "ts" // in millis.
            private const val MESSAGE_KEY = "msg" // message generates at runtime.
            private const val NUMERIC_KEY = "num" // message numeric
            private const val UID_KEY = "uid" // uid user
        }

    }

    companion object {
        @JvmField
        var isCrashltyicsInitialized :Boolean = false

        private const val TAG = "Logger"
        private val lockLogger = AtomicBoolean(false)
        private const val BUFFER_LOG_SIZE_MAX = 10
        private var mContext: Context? = null
        private var sFirebaseDb: IFirebaseDb? = null

        private var sCountDownHandler: Handler? = null
        private var sCountDownRunnable: Runnable? = null
        private val sLogsList: ConcurrentLinkedQueue<LogItem> = ConcurrentLinkedQueue<LogItem>()
        private var mCounterStarted = false
        private var mActivityCreated = false
        private var mServiceCreated = false
        private var mServiceMode: ServiceMode? = null


        private fun sendLogsToFirebase() {
            if (mContext == null) {
                return
            }
            // post the elements in sLogList to Firebase
            if (lockLogger.compareAndSet(false, true)) {
                Log.d(TAG, "size logs: " + logs.size)
                if (sFirebaseDb != null) {
                    sFirebaseDb?.writeLog(mContext, sLogsList)
                }
                clear()
                lockLogger.set(false)
            }
        }

        /**
         * Kill all instances (excepted the object itself)
         */
        private fun kill() {
            if (sCountDownHandler != null && sCountDownRunnable != null) {
                sCountDownHandler?.removeCallbacks(sCountDownRunnable!!)
                lockLogger.set(false)
                mCounterStarted = false
            }
        }

        /**
         *
         * @return
         */
        val logs: ConcurrentLinkedQueue<LogItem>
            get() = sLogsList

        /**
         * Log error only
         *
         * @param logEnum
         * @param tag
         * @param trace
         */
        @JvmStatic
        fun e(logEnum: LogEnum, tag: String, trace: String) {
            Log.e(tag, logEnum.text + ", trace : " + trace)
            val log = EllcieLog(logEnum.name, tag + ": " + logEnum.text, trace)
            addLog(log)
        }

        /**
         *
         * @param logEnum
         * @param tag
         * @param numericValue
         */
        @JvmStatic
        fun e(logEnum: LogEnum, tag: String?, numericValue: Number) {
            Log.e(tag, logEnum.text + ", numeric value : " + numericValue)
            val log = tag?.let { EllcieLog(logEnum.name, it, logEnum.text, numericValue) }
            addLog(log)
        }

        /**
         *
         * @param logEnum
         * @param tag
         * @param trace
         * @param e
         */
        @JvmStatic
        fun e(logEnum: LogEnum, tag: String?, trace: String, e: Exception) {
            Log.e(tag, logEnum.text + ", trace : " + trace + " " + e.localizedMessage)
            tag?.let {
                e.localizedMessage?.let { localizedMessage ->
                    addLog(EllcieLog(logEnum.name, tag, logEnum.text, localizedMessage))
                }
            }
        }

        /**
         * Log error only
         *
         * @param logEnum
         * @param tag
         */
        @JvmStatic
        fun e(logEnum: LogEnum, tag: String) {
            Log.e(tag, logEnum.text)
            val log = EllcieLog(logEnum.name, tag, logEnum.text)
            addLog(log)
        }

        /**
         * Log error only
         *
         * @param tag
         * @param text
         */
        @JvmStatic
        fun e(tag: String, text: String) {
            addLog("$tag, $text")
            Log.e(tag, text)
        }

        /**
         * Log error only
         *
         * @param tag
         * @param text
         * @param e
         */
        @JvmStatic
        fun e(tag: String?, text: String, e: Throwable) {
            addLog(", $text " + e.localizedMessage)
            Log.e(tag, text + " " + e.localizedMessage)
        }

        /**
         * Log info only (connection to the glass, trip started, trip stopped, params displayed, glass dosconnected)
         *
         * @param logEnum
         * @param tag
         * @param trace
         */
        @JvmStatic
        fun i(logEnum: LogEnum, tag: String, trace: String) {
            Log.i(tag, logEnum.text + ", trace : " + trace)
            val log = EllcieLog(logEnum.name, tag + ": " + logEnum.text, trace)
            addLog(log)
        }

        /**
         *
         * @param logEnum
         * @param tag
         * @param numericValue
         */
        @JvmStatic
        fun i(logEnum: LogEnum, tag: String?, numericValue: Number) {
            Log.i(tag, logEnum.text + ", numeric value : " + numericValue)
            val log = tag?.let { EllcieLog(logEnum.name, it, logEnum.text, numericValue) }
            addLog(log)
        }

        /**
         * Log info only (connection to the glass, trip started, trip stopped, params displayed, glass dosconnected)
         *
         * @param logEnum
         * @param tag
         */
        @JvmStatic
        fun i(logEnum: LogEnum, tag: String?) {
            Log.i(tag, logEnum.text)
            val log = tag?.let { EllcieLog(logEnum.name, it, logEnum.text) }
            addLog(log)
        }

        @ExperimentalStdlibApi
        @JvmStatic
        fun ehLogI(reference: String?, tag: String?, message: String) {
            Log.i(tag, message)
            if (reference != null && tag != null) {
                val log = EllcieLog(reference, tag, message)
                addLog(log)
            }
        }

        @JvmStatic
        fun ehLogI(reference: String?, tag: String?, message: String, runtimeMessage: String) {
            Log.i(tag, message)
            if(reference != null && tag != null) {
                val log = EllcieLog(reference, tag, message, runtimeMessage)
                addLog(log)
            }
        }

        @JvmStatic
        fun ehLogI(reference: String?, tag: String?, message: String, value: Number?) {
            Log.i(tag, message)
            if(reference != null && tag != null) {
                val log = EllcieLog(reference, tag, message, value)
                addLog(log)
            }
        }

        @JvmStatic
        fun ehLogW(reference: String, tag: String, message: String) {
            Log.w(tag, message)
            val log = EllcieLog(reference, tag, message)
            addLog(log)
        }

        @JvmStatic
        fun ehLogW(reference: String, tag: String, message: String, runtimeMessage: String) {
            Log.w(tag, message)
            val log = EllcieLog(reference, tag, message, runtimeMessage)
            addLog(log)
        }

        @JvmStatic
        fun ehLogE(reference: String, tag: String, message: String) {
            Log.e(tag, message)
            val log = EllcieLog(reference, tag, message)
            addLog(log)

        }

        @JvmStatic
        fun ehLogE(reference: String, tag: String, message: String, runtimeMessage: String) {
            Log.e(tag, message)
            val log = EllcieLog(reference, tag, message, runtimeMessage)
            addLog(log)
        }

        /**
         * Log info only (connection to the glass, trip started, trip stopped, params displayed, glass dosconnected)
         *
         * @param ellcieLog
         */
        fun i(ellcieLog: EllcieLog?) {
            addLog(ellcieLog)
        }

        /**
         * Log info only
         *
         * @param tag
         * @param text
         */
        fun i(tag: String, text: String) {
            addLog("$tag, $text")
            Log.i(tag, text)
        }

        /**
         * Log warning only.
         *
         * @param logEnum
         * @param tag
         */
        @Suppress("unused")
        @JvmStatic
        fun w(logEnum: LogEnum, tag: String?) {
            Log.w(tag, logEnum.text)
            val log = tag?.let { EllcieLog(logEnum.name, it, logEnum.text) }
            addLog(log)
        }

        /**
         *
         * @param logEnum
         * @param tag
         * @param numericValue
         */
        @Suppress("unused")
        @JvmStatic
        fun w(logEnum: LogEnum, tag: String?, numericValue: Number) {
            Log.w(tag, logEnum.text + ", numeric value : " + numericValue)
            val log = tag?.let { EllcieLog(logEnum.name, it, logEnum.text, numericValue) }
            addLog(log)
        }

        /**
         *
         * @param logEnum
         * @param tag
         * @param trace
         * @param numericValue
         */
        @Suppress("unused")
        @JvmStatic
        fun w(logEnum: LogEnum, tag: String?, trace: String, numericValue: Number?) {
            Log.w(tag, logEnum.text + ", trace : " + trace)
            val log = tag?.let { EllcieLog(logEnum.name, it, logEnum.text, numericValue) }
            addLog(log)
        }

        /**
         * Log warning only.
         *
         * @param logEnum
         * @param tag
         * @param trace
         */
        @Suppress("unused")
        @JvmStatic
        fun w(logEnum: LogEnum, tag: String, trace: String) {
            Log.w(tag, logEnum.text + ", trace : " + trace)
            val log = EllcieLog(logEnum.name, tag + ": " + logEnum.text, trace)
            addLog(log)
        }

        /**
         * Log warning only.
         *
         * @param text
         */
        @JvmStatic
        fun w(tag: String, text: String) {
            addLog("$tag, $text")
            Log.w(tag, text)
        }

        /**
         *
         * @param tag
         * @param text
         */
        @JvmStatic
        fun v(tag: String, text: String) {
            addLog("$tag, $text")
            Log.v(tag, text)
        }

        /**
         *
         * @param tag
         * @param text
         */
        @JvmStatic
        fun d(tag: String?, text: String) {

            // Just a test if it can fix the out of memory error during a long trip
            //addLog(tag + ", " + text);
            Log.d(tag, text)
        }

        /**
         * canBeModified -> indicates if sLogsList can be modified.
         * true -> can be modified
         * false -> can'T be modified.
         *
         * @return
         */
        private fun canBeModified(): Boolean {
            return !lockLogger.get()
        }

        @JvmStatic
        fun clear() {
            sLogsList.clear()
        }

        private fun addLog(log: String) {
            if (isCrashltyicsInitialized) {
                val crashlytics = FirebaseCrashlytics.getInstance()
                crashlytics.log(log)
            }
        }

        /**
         *
         * @param log
         */
        private fun addLog(log: EllcieLog?) {
            if(mServiceMode == null) {
                return
            }
            if (isCrashltyicsInitialized) {
                val crashlytics = FirebaseCrashlytics.getInstance()
                if (log?.message != null) {
                    crashlytics.log(log.message)
                } else {
                    log?.runtimeTrace?.let { crashlytics.log(it) }
                }
            }
            if (canBeModified()) {
                if (log != null) {
                    when {
                        log.runtimeTrace.isNotEmpty() -> {
                            sLogsList.add(mServiceMode?.let { LogItem(it, log.reference, log.runtimeTrace, log.timestamp) })
                        }
                        log.value != null -> {
                            sLogsList.add(mServiceMode?.let { LogItem(it, log.reference, log.value!!, log.timestamp) })
                        }
                        else -> {
                            sLogsList.add(mServiceMode?.let { LogItem(it, log.reference, "", log.timestamp) })
                        }
                    }
                    if (BUFFER_LOG_SIZE_MAX <= sLogsList.size) {
                        sendLogsToFirebase()
                    }
                }
            }
        }

        private fun onDestroy() {
            Log.d(TAG, "onDestroy()")
            sendLogsToFirebase() // force push log to firebase
            kill() // kill countdown and unlock the lock logger.
        }

        @JvmStatic
        fun activityCreated() {
            Log.d(TAG, "activityCreated()")
            mActivityCreated = true
        }

        @JvmStatic
        fun activityDestroyed() {
            Log.d(TAG, "activityDestroyed()")
            mActivityCreated = false
            if (!mServiceCreated) {
                onDestroy()
            }
        }

        /**
         *
         * Initialization method for logger.
         * It should be called as soon as you want to regularly send App Logs (with LogEnum)
         * to Firebase
         *
         */
        @JvmStatic
        fun prepare(periodInSeconds: Int, serviceMode: ServiceMode, context: Context, firebaseDb: IFirebaseDb) {
            // prepare the timer that sends the logs every periodInSeconds seconds
            if (mCounterStarted) {
                // the counter has already been started
                return
            }
            mServiceMode = serviceMode
            mContext = context
            sFirebaseDb = firebaseDb


            // creates and starts a new thread set up as a looper
            val thread = HandlerThread("MyHandlerThread")
            thread.start()

            sCountDownHandler = Handler(thread.looper)
            sCountDownRunnable = object : Runnable {
                override fun run() {
                    sendLogsToFirebase()
                    sCountDownHandler?.postDelayed(this, (periodInSeconds * 1000).toLong())
                }
            }
            sCountDownHandler?.postDelayed(sCountDownRunnable as Runnable, (periodInSeconds * 1000).toLong())
        }


    }

}

