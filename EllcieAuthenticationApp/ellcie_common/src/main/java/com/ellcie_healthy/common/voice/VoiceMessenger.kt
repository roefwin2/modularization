package com.ellcie_healthy.common.utils.com.ellcie_healthy.common.voice


import android.content.Context
import android.speech.tts.TextToSpeech
import com.ellcie_healthy.common.utils.com.ellcie_healthy.common.loggers.LogEnum
import com.ellcie_healthy.common.utils.com.ellcie_healthy.common.loggers.Logger
import com.ellcie_healthy.ellcie_common.R
import java.util.*


/**
 * Created by Julien on 11/05/2021
 */
class VoiceMessageManager private constructor(context: Context) {
    private var mInit = false
    private var mTextToSpeech: TextToSpeech? = null
    private var isSuspended = false // set to true to avoid playing sound (optician test uses it)

    /**
     *
     * @param context
     */
    private fun init(context: Context) {
        Logger.d(TAG, "init")
        if (mInit) {
            return
        }
        mTextToSpeech = TextToSpeech(context.applicationContext) { status ->
            if (status == 0) {
                Logger.d(TAG, "TextToSpeech initialized")
                mInit = true
                autocheckLanguage()
                setSpeed()
            } else {
                Logger.e(LogEnum.SEA0A1, TAG, "code error : $status")
            }
        }
    }

    /**
     * Autocheck of the system language. United States by default.
     */
    private fun autocheckLanguage() {
        val lang = Locale.getDefault().language
        if (lang == Locale("fr").language) {
            if (mTextToSpeech!!.isLanguageAvailable(Locale.FRANCE) > -1) {
                setLanguage(Locale.FRANCE)
            }
        } else {
            if (mTextToSpeech!!.isLanguageAvailable(Locale.US) > -1) {
                setLanguage(Locale.US)
            }
        }
    }

    private fun setSpeed() {
        mTextToSpeech!!.setSpeechRate(0.9f)
    }

    private fun speakText(text: String?): Int {
        Logger.d(TAG, "speakText")
        return if (mInit && !isSuspended) {
            mTextToSpeech!!.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        } else {
            Logger.e(LogEnum.SEA0A2, TAG)
            -1
        }
    }

    /**
     * Emit a voice message to warn about a calibration/recalibration error
     *
     * @param context
     */
    @Suppress("unused")
    fun speechGenericStopTripError(context: Context) {
        Logger.d(TAG, "speechGenericStopTripError")
        if (mInit) {
            val res = speakText(context.getString(R.string.vocale_message_generic_error))
            Logger.d(TAG, "speechGenericStopTripError: res: $res")
        } else {
            Logger.e(LogEnum.SEA0A2, TAG)
        }
    }

    /**
     * Emit a voice message to warn about a sensor error
     *
     */
    @Suppress("unused")
    fun speechSensorError() {
        if (!mInit) {
            Logger.e(LogEnum.SEA0A2, TAG)
        }
    }




    fun speechAlertConfirmed(context: Context) {
        if (mInit) {
            val res = speakText(context.getString(R.string.vocale_message_alert_confirmed))
            Logger.d(TAG, "speechAlertConfirmed: res: $res")
        } else {
            Logger.e(LogEnum.SEA0A2, TAG)
        }
    }

    fun speechSosCountdownInitiatedByMobile(context: Context) {
        if (mInit) {
            val res = speakText(context.getString(R.string.vocale_message_sos_countdown_initiated_by_mobile))
            Logger.d(TAG, "speechSosCountdownInitiatedByMobile: res: $res")
        } else {
            Logger.e(LogEnum.SEA0A2, TAG)
        }
    }

    fun speechSosCountdownInitiatedByGlasses(context: Context) {
        if (mInit) {
            val res = speakText(context.getString(R.string.vocale_message_sos_countdown_initiated_by_glasses))
            Logger.d(TAG, "speechPauseAboutTrip: res: $res")
        } else {
            Logger.e(LogEnum.SEA0A2, TAG)
        }
    }

    fun speechFallCountdownInitiated(context: Context) {
        if (mInit) {
            val res = speakText(context.getString(R.string.vocale_message_fall_countdown_initiated))
            Logger.d(TAG, "speechPauseAboutTrip: res: $res")
        } else {
            Logger.e(LogEnum.SEA0A2, TAG)
        }
    }

    private fun setLanguage(language: Locale) {
        Logger.d(TAG, "setLanguage: language: $language")
        mTextToSpeech!!.language = language
    }

    fun stopSpeaking() {
        mTextToSpeech!!.stop()
    }

    companion object {
        private const val TAG = "VoiceMessageManager"
        private var mInstance: VoiceMessageManager? = null
        fun getInstance(context: Context): VoiceMessageManager {
            if (mInstance == null) {
                mInstance = VoiceMessageManager(context)
            }
            return mInstance as VoiceMessageManager
        }
    }

    init {
        init(context)
    }
}
