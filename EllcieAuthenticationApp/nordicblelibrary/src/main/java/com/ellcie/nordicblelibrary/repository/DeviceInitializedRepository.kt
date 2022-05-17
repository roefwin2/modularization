package com.ellcie.nordicblelibrary.repository

import com.ellcie.nordicblelibrary.services.MyBoundService
import com.ellcie_healthy.ble_library.ble.profile.command.data.SensorType
import com.ellcie_healthy.ble_library.ble.profile.measure.data.SensorData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Repository to collect all listener
 * @param binder to get the listener
 */
interface DeviceRepository

data class DeviceInitializedRepository(private val binder: MyBoundService.MyBinder) :
    DeviceRepository {
    val battery: StateFlow<Int>
        get() = binder.battery

    val isCharging: StateFlow<Boolean>
        get() = binder.isCharging

    val sensors: StateFlow<SensorData<Double>>
        get() = binder.sensors

    suspend fun lokalizeMe() = binder.lokalizeMe()
    suspend fun disableFall() = binder.disableFall()
    suspend fun engageSos() = binder.engageSos()
    suspend fun cancelSos() = binder.cancelSos()
    suspend fun disableAlgo(disable: Boolean) = binder.disableAlgo(disable)
    suspend fun setUpStreaming(sensorsList: ArrayList<SensorType>) =
        binder.setupStreaming(sensorsList)

    suspend fun setTripStatus(tripEnable: Boolean) = binder.setTripStatus(tripEnable)
}
