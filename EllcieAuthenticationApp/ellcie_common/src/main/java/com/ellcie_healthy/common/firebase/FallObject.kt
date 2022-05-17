package com.ellcie_healthy.common.utils.com.ellcie_healthy.common.firebase


import com.ellcie_healthy.common.utils.com.ellcie_healthy.common.loggers.Logger
import com.ellcie_healthy.common.utils.com.ellcie_healthy.common.models.Contact
import com.google.firebase.database.DataSnapshot
import java.util.*


/**
 * Used to manage the fall alerts process.
 */
class FallObject {
    var triggerDate: Long = 0
    var triggerType: String? = ""
    private var mTriggerDevice: String? = ""
    private var mEndDate: Long = 0
    private var mEndType: String? = ""
    var endDevice: String? = ""
    private var mRescueTriggerDate: Long = 0
    private var mRescueEndDate: Long = 0
    var rescueEndType: String? = ""
    private var mLastLocationAlreadyStoredInServer = false
    private var mLatitudePosition: String? = ""
    private var mLongitudePosition: String? = ""
    private var mHelpers: MutableList<Contact> = ArrayList<Contact>()
    private val mEvents: MutableList<FallEvent> = ArrayList<FallEvent>()

    constructor()
    constructor(dataSnapshot: DataSnapshot) {
        Logger.d(TAG, "FallObject: constructor called")
        mHelpers = ArrayList<Contact>()
        if (dataSnapshot.hasChild("phase1Detection")) {
            val phase1 = dataSnapshot.child("phase1Detection")
            if (phase1.hasChild("triggerDate")) {
                triggerDate = phase1.child("triggerDate").value as Long
            }
            if (phase1.hasChild("triggerType")) {
                triggerType = phase1.child("triggerType").value as String?
            }
            if (phase1.hasChild("triggerDevice")) {
                mTriggerDevice = phase1.child("triggerDevice").value as String?
            }
            if (phase1.hasChild("gps")) {
                Logger.d(TAG, "FallObject: gps node found")
                mLatitudePosition = phase1.child("gps").child("latitude").value as String?
                mLongitudePosition = phase1.child("gps").child("longitude").value as String?
                if (phase1.child("gps").hasChild("elapsedTimeSinceLastGpsReceptionMs")) {
                    mLastLocationAlreadyStoredInServer = true
                    mElapsedTimeSinceLastGpsReceptionMs = (phase1.child("gps").child("elapsedTimeSinceLastGpsReceptionMs").value as Long?)!!
                } else {
                    mLastLocationAlreadyStoredInServer = false
                }
            }
            if (phase1.hasChild("endDate")) {
                mEndDate = phase1.child("endDate").value as Long
            }
            if (phase1.hasChild("endType")) {
                mEndType = phase1.child("endType").value as String?
            }
            if (phase1.hasChild("endDevice")) {
                endDevice = phase1.child("endDevice").value as String?
            }
        }
        if (dataSnapshot.hasChild("phase2Rescue")) {
            val phase2 = dataSnapshot.child("phase2Rescue")
            if (phase2.hasChild("triggerDate")) {
                mRescueTriggerDate = phase2.child("triggerDate").value as Long
            }
            if (phase2.hasChild("endDate")) {
                mRescueEndDate = phase2.child("endDate").value as Long
            }
            if (phase2.hasChild("endType")) {
                rescueEndType = phase2.child("endType").value as String?
            }
            if (dataSnapshot.hasChild("phase2Rescue/events")) {
                val eventsSnap = dataSnapshot.child("phase2Rescue/events")
                val iterator = eventsSnap.children
                for (snapshot in iterator) {
                    val fallEvent = FallEvent(snapshot)
                    mEvents.add(fallEvent)
                }
            }
            if (dataSnapshot.hasChild("phase2Rescue/helpers")) {
                val contactsSnap = dataSnapshot.child("phase2Rescue/helpers")
                val iterator = contactsSnap.children
                for (snapshot in iterator) {
                    mHelpers.add(Contact(snapshot))
                }
            }
        }
    }

    fun alertIsCanceled(): Boolean {
        return mEndType == "CANCEL" || rescueEndType == "CANCEL"
    }

    fun emergencyContacted(): Boolean {
        return rescueEndType == "EMERGENCY_CONTACTED"
    }// check if the event type is an vitaris's event.// for each events of the current alert

    // if ack by Vitaris, the last
    val vitarisReasonAck: FallEvent?
        get() {
            if (!acquittedByVitaris()) {
                return null
            }

            // if ack by Vitaris, the last
            val allEvents: Array<VitarisEvent> = VitarisEvent.values()
            for (fallEvent in mEvents) {
                // for each events of the current alert
                val currentType: String = fallEvent.eventType
                for (anEvent in allEvents) {
                    // check if the event type is an vitaris's event.
                    if (anEvent.value == currentType) {
                        return fallEvent
                    }
                }
            }
            return null
        }

    fun retrieveHelperAck(): Contact? {
        if (!acquittedByHelper()) {
            return null
        }

        //Retrieve first acquitement
        for (fallEvent in mEvents) {
            if (fallEvent.eventType == rescueEndType) {
                return fallEvent.payload?.let { retrieveHelperByKey(it) }
            }
        }
        return null
    }

    fun acquittedByVitaris(): Boolean {
        return rescueEndType == "ACK_VITARIS"
    }

    fun acquittedByHelper(): Boolean {
        return rescueEndType == "ACK_MAIL" || rescueEndType == "ACK_SMS" || rescueEndType == "ACK_HI"
    }

    fun hasRescueEngaged(): Boolean {
        return mEndDate != 0L && (mEndType == "TIMEOUT" || mEndType == "MANUAL_CONFIRM") && rescueEndType == ""
    }

    val isVitarisContacted: Boolean
        get() {
            Logger.d(TAG, "isVitarisContacted: events size: " + mEvents.size)
            if (mEvents.size == 0) {
                Logger.d(TAG, "isVitarisContacted: no events")
                return false
            }
            for (i in mEvents.indices) {
                Logger.d(TAG, "isVitarisContacted: i $i")
                if (mEvents[i].eventType == "VITARIS_CONTACTED") {
                    Logger.d(TAG, "isVitarisContacted: YES")
                    Logger.d(TAG, "isVitarisContacted: event type: " + mEvents[i].eventType)
                    return true
                }
            }
            Logger.d(TAG, "isVitarisContacted: NO")
            return false
        }

    fun retrieveLastHelperContacted(): Contact? {
        Logger.d(TAG, "retrieveLastHelperContacted: events size: " + mEvents.size)
        if (mEvents.size == 0) {
            Logger.d(TAG, "retrieveLastHelperContacted: no events")
            return null
        }
        var contact: Contact? = null
        for (i in mEvents.indices) {
            Logger.d(TAG, "retrieveLastHelperContacted: i $i")
            if (mEvents[i].eventType == "MAIL_SENT" || mEvents[i].eventType == "SMS_SENT") {
                Logger.d(TAG, "retrieveLastHelperContacted: event type: " + mEvents[i].eventType)
                contact = mEvents[i].payload?.let { retrieveHelperByKey(it) }
            }
        }
        Logger.d(TAG, "retrieveLastHelperContacted: contact : $contact")
        return contact
    }

    private fun retrieveHelperByKey(helperKey: String): Contact? {
        for (helper in mHelpers) {
            if (helper.contactId == helperKey) {
                return helper
            }
        }
        return null
    }

    fun retrieveHelperByPriority(priority: Int): Contact? {
        for (helper in mHelpers) {
            if (helper.priority == priority) {
                return helper
            }
        }
        return null
    }

    fun convertToFirebase(): Map<String, Any> {
        Logger.d(TAG, "convertToFirebase()")
        val phase1: MutableMap<String, Any?> = HashMap()
        val gpsMap: MutableMap<String, Any?> = HashMap()
        var phase2: MutableMap<String?, Any?>? = null
        phase1["triggerDate"] = triggerDate
        phase1["triggerType"] = triggerType
        phase1["triggerDevice"] = mTriggerDevice

        // set location.
       //val now = System.currentTimeMillis()
        //var elapsedTimeSinceLastGpsReceptionMs: Long
        gpsMap["latitude"] = mLatitudePosition
        gpsMap["longitude"] = mLongitudePosition
        gpsMap["elapsedTimeSinceLastGpsReceptionMs"] = mElapsedTimeSinceLastGpsReceptionMs
        phase1["gps"] = gpsMap
        if (mEndDate != 0L) {
            phase1["endDate"] = mEndDate
        }
        if (mEndType != "") {
            phase1["endType"] = mEndType
        }
        if (endDevice != "") {
            phase1["endDevice"] = endDevice
        }
        if (mRescueEndDate != 0L || mRescueTriggerDate != 0L || rescueEndType != "") {
            phase2 = HashMap()
            if (mRescueEndDate != 0L) {
                phase2["endDate"] = mRescueEndDate
            }
            if (rescueEndType != "") {
                phase2["endType"] = rescueEndType
            }
            if (mRescueTriggerDate != 0L) {
                phase2["triggerDate"] = mRescueTriggerDate
            }
        }
        val root: MutableMap<String, Any> = HashMap()
        root["phase1Detection"] = phase1
        if (phase2 != null) {
            root["phase2Rescue"] = phase2
        }
        return root
    }

    fun setRescueEndDate(timestampMs: Long) {
        mRescueEndDate = timestampMs
    }

    fun setEndType(type: String?) {
        mEndType = type
    }

    fun setEndDate(timestampMs: Long) {
        mEndDate = timestampMs
    }

    fun setTriggerDevice(device: String?) {
        mTriggerDevice = device
    }

    /**
     *
     * @param latitude
     * @param longitude
     * @param elapsedTimeSinceLastGpsReceptionMs
     */
    fun setLatLongPosition(latitude: String, longitude: String, elapsedTimeSinceLastGpsReceptionMs: Long) {
        Logger.d(TAG, "setLatLongPosition()")
        if (!mLastLocationAlreadyStoredInServer && mLatitudePosition == "" && mLongitudePosition == "") {
            mLatitudePosition = latitude
            mLongitudePosition = longitude
            mElapsedTimeSinceLastGpsReceptionMs = elapsedTimeSinceLastGpsReceptionMs
        }
    }

    fun clear() {
        Logger.d(TAG, "clear()")
        triggerDate = 0
        triggerType = ""
        mTriggerDevice = ""
        mEndDate = 0
        mEndType = ""
        endDevice = ""
        mRescueTriggerDate = 0
        mRescueEndDate = 0
        rescueEndType = ""
        mLongitudePosition = ""
        mLatitudePosition = ""
        mElapsedTimeSinceLastGpsReceptionMs = 0
        mLastLocationAlreadyStoredInServer = false
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("mTriggerDate: ")
        sb.append(triggerDate)
        sb.append("\n")
        sb.append("mTriggerType: ")
        sb.append(triggerType)
        sb.append("\n")
        sb.append("mTriggerDevice: ")
        sb.append(mTriggerDevice)
        sb.append("\n")
        sb.append("mEndDate: ")
        sb.append(mEndDate)
        sb.append("\n")
        sb.append("mEndType: ")
        sb.append(mEndType)
        sb.append("\n")
        sb.append("mEndDevice: ")
        sb.append(endDevice)
        sb.append("\n")
        for (fallEvent in mEvents) {
            sb.append("event: ")
            sb.append(fallEvent.timestamp)
            sb.append(" : ")
            sb.append(fallEvent.timestamp)
            sb.append("\n")
        }
        return sb.toString()
    }

    companion object {
        var mElapsedTimeSinceLastGpsReceptionMs: Long = 0
        private const val TAG = "FallObject"
    }
}
