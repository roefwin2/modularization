package com.ellcie.ellcieauthenticationapp.network.repositories


import android.content.Context
import com.ellcie.nordicblelibrary.EllcieBleManager
import com.ellcie.nordicblelibrary.ResearchBleManager
import com.ellcie_healthy.ble_library.ble.profile.command.data.SensorType

interface BleRepository {

}

class BleRepositoryImpl(private val bleManager: ResearchBleManager, private val context: Context) :
    BleRepository {

    val bleDeviceConnection get() = bleManager.deviceConnection

    fun connectDevice(macAddress: String, clazz: Class<*>) =
        bleManager.connect(macAddress, clazz, context)

    fun localizeMe() = bleManager.localizeMe()
    suspend fun disableAlgo(disable : Boolean) = bleManager.disableAlgo(disable)
    fun setupStreaming(sensorList : ArrayList<SensorType>) = bleManager.setupStreaming(sensorList)
    fun setTripStatus(tripEnable: Boolean) = bleManager.setTripStatus(tripEnable)

    fun getBatteryLevel() = bleManager.getBatteryLevel()

    fun getChargingState() = bleManager.getChargingState()

    fun getSensorsData() = bleManager.getSensorData()

    fun disconnectDevice() = bleManager.disconnect(context)

}
