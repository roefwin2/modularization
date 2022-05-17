package com.ellcie_healthy.common.utils.com.ellcie_healthy.common.firebase

import android.content.Context

import com.ellcie_healthy.common.callbacks.EllcieCommonCallbackGetGeneric
import com.ellcie_healthy.common.callbacks.EllcieCommonCallbackGetString
import com.ellcie_healthy.common.utils.com.ellcie_healthy.common.loggers.Logger
import com.google.firebase.database.DataSnapshot
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

interface IFirebaseDb {
    fun writeGpsPosition(tripId: String?, serialNumber: String?, timestamp: String?, latitude: String?, longitude: String?)
    fun writeBestMeanValue(tripId: String?, serialNumber: String?, firmwareVersion: String?, bestMeanValue: String?,isMandatoryFirmware : Boolean,algoSensitivityLevel :Int)
    fun uploadSensorsDataFile(context: Context?, pathFile: String?, tripId: String?, userId: String?, serialNumber: String?)
    fun uploadFallDataFile(context: Context?, pathFile: String?, userId: String?, serialNumber: String?, timestamp: String?)
    fun uploadAlgoEventFile(context: Context?, pathFile: String?, tripId: String?, userId: String?)
    fun uploadGatheringFile(context: Context?, pathFile: String?)
    fun uploadStreamingOpticianFile(pathFile: String?, userId: String?, serialNumber: String?)
    fun pushGlassesLog(context: Context?, glassesLog: GlassesLogFb)
    fun writeLog(context: Context?, logItems: ConcurrentLinkedQueue<Logger.LogItem>?)
    fun writeFirmwareVersionForStreaming(userId: String?, serialNumber: String?, tripId: String?)
    fun pushBleConnectionStatus(serialNumber: String?, status: Boolean)
    fun readUserRole(userId: String?, cbDone: EllcieCommonCallbackGetString?)
    fun updateBatteryLevel(serialNumber: String?, batteryLevel: Int)
    fun updateBatteryPowerStateStatus(serialNumber: String?, status: Boolean)
    fun updateLastGlassesUsed(serialNumber: String?)
    fun readMobileConfig(manufacturer: String?, product: String?, callback: EllcieCommonCallbackGetGeneric<DataSnapshot?>?)

    /*
    Fall
     */
    fun writeFallObject(dico: Map<String?, Any?>?, timestampMs: String?)
    fun updateFallStatus(status: Byte, timestampMs: Long)
    fun writeLastFallAlert(timestampMs: Long)
    fun listenOngoingRescueAlert(cb: EllcieCommonCallbackGetGeneric<FallObject?>?)
    fun removeOngoingRescueAlertListener()
    fun getLatestFallAlert(cb: EllcieCommonCallbackGetGeneric<FallObject?>?)
    fun serviceCreated()
    fun serviceDestroyed()
}
