package com.ellcie.nordicblelibrary

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import com.ellcie.nordicblelibrary.services.ConnectionObserverAdapter
import com.ellcie_healthy.ble_library.ble.profile.battery.callback.BatteryLevelDataCallback
import com.ellcie_healthy.ble_library.ble.profile.battery.callback.BatteryPowerStateDataCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.ble.livedata.ObservableBleManager
import java.util.*

/**
 * Battery Service UUID.
 */
private val BATTERY_SERVICE_UUID = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb")

/**
 * Battery Level characteristic UUID.
 */
private val BATTERY_LEVEL_CHARACTERISTIC_UUID =
    UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb")

/**
 * Battery Power State characteristic UUID.
 */
private val BATTERY_POWER_STATE_CHARACTERISTIC_UUID =
    UUID.fromString("00002A1A-0000-1000-8000-00805f9b34fb")

class NordicBleManager(
    @ApplicationContext context: Context,
    scope: CoroutineScope,
) : ObservableBleManager(context) {

    private var batteryLevelCharacteristic: BluetoothGattCharacteristic? = null
    private var powerStatusCharacteristic: BluetoothGattCharacteristic? = null

    private val data = MutableStateFlow(BPSData())
    val dataHolder =  ConnectionObserverAdapter<BPSData>()

    private val batteryLevelDataCallback = object  : BatteryLevelDataCallback() {
        override fun onBatteryLevel(device: BluetoothDevice, batteryLevel: Int) {
            TODO("onBattery no implemented")
        }
    }

    private val batteryPowerStateDataCallback = object : BatteryPowerStateDataCallback(){
        override fun onBatteryPowerState(
            device: BluetoothDevice,
            plug: Boolean?,
            charging: Boolean?
        ) {
            TODO("Not yet implemented")
        }
    }

    init {
        setConnectionObserver(dataHolder)

        data.onEach {
            dataHolder.setValue(it)
        }.launchIn(scope)
    }

    override fun log(priority: Int, message: String) {
        Log.d(priority.toString(), message)
    }

    override fun getMinLogPriority(): Int {
        return Log.VERBOSE
    }

    override fun getGattCallback(): BleManagerGattCallback {
        return BloodPressureManagerGattCallback()
    }

    fun connectToDevice(device: BluetoothDevice){
        connect(device)
            .useAutoConnect(false)
            .retry(3, 100)
            .await()
    }

    private inner class BloodPressureManagerGattCallback : BleManagerGattCallback() {

        @OptIn(ExperimentalCoroutinesApi::class)
        override fun initialize() {
            super.initialize()
            setIndicationCallback(powerStatusCharacteristic).with(batteryPowerStateDataCallback)
            setNotificationCallback(batteryLevelCharacteristic).with(batteryLevelDataCallback)

            enableIndications(powerStatusCharacteristic).await()
            enableNotifications(batteryLevelCharacteristic).await()
        }

        override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            gatt.getService(BATTERY_SERVICE_UUID)?.run {
                batteryLevelCharacteristic = getCharacteristic(BATTERY_LEVEL_CHARACTERISTIC_UUID)
                powerStatusCharacteristic = getCharacteristic(
                    BATTERY_POWER_STATE_CHARACTERISTIC_UUID)
            }
            return batteryLevelCharacteristic != null && powerStatusCharacteristic!= null
        }

        override fun onServicesInvalidated() {
            batteryLevelCharacteristic = null
            powerStatusCharacteristic = null
        }
    }
}

data class BPSData(
    val batteryLevel: Int? = null,
    val cuffPressure: Float = 0f,
    val unit: Int = 0,
    val pulseRate: Float? = null,
    val userID: Int? = null,
    val calendar: Calendar? = null,
    val systolic: Float = 0f,
    val diastolic: Float = 0f,
    val meanArterialPressure: Float = 0f,
)