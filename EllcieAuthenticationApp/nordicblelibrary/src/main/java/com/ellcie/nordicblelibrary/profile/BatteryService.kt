package com.ellcie.nordicblelibrary.profile

import android.bluetooth.BluetoothDevice
import com.ellcie.nordicblelibrary.NewEHBleManager
import com.ellcie_healthy.ble_library.ble.profile.BleService
import com.ellcie_healthy.ble_library.ble.profile.EHBleManager
import com.ellcie_healthy.ble_library.ble.profile.battery.BatteryManagerCallbacks
import com.ellcie_healthy.ble_library.ble.profile.battery.callback.BatteryLevelDataCallback
import com.ellcie_healthy.ble_library.ble.profile.battery.callback.BatteryPowerStateDataCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.*

class BatteryService(
    /**
     * Battery Service UUID.
     */
    BATTERY_SERVICE_UUID: UUID,
    newEHBleManager: NewEHBleManager
) : NordicBleService<BatteryManagerCallbacks>(
    newEHBleManager, BATTERY_SERVICE_UUID, "Battery Service"
) {


    /**
     * Battery Level characteristic UUID.
     */
    private val BATTERY_LEVEL_CHARACTERISTIC_UUID: UUID =
        UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb")

    /**
     * Battery Power State characteristic UUID.
     */
    private val BATTERY_POWER_STATE_CHARACTERISTIC_UUID: UUID =
        UUID.fromString("00002A1A-0000-1000-8000-00805f9b34fb")

    private val _levelState : MutableStateFlow<Int> = MutableStateFlow(-1)
    val levelState : StateFlow<Int> get() = _levelState


    //batteryLevel
    private val batteryLevelDataCallback =
        object : BatteryLevelDataCallback() {
            override fun onBatteryLevel(device: BluetoothDevice, batteryLevel: Int) {
                _levelState.value = batteryLevel
            }
        }

    private val level: NordicBleCharacteristic<BatteryLevelDataCallback> =
        NordicBleCharacteristic(
            BATTERY_LEVEL_CHARACTERISTIC_UUID,
            "Battery Level",
            batteryLevelDataCallback
        )


    //power state
    private val batteryPowerStateDataCallback =
        object : BatteryPowerStateDataCallback() {
            override fun onBatteryPowerState(
                device: BluetoothDevice,
                plug: Boolean?,
                charging: Boolean?
            ) {
            }
        }

    private val power: NordicBleCharacteristic<BatteryPowerStateDataCallback> =
        NordicBleCharacteristic(
            BATTERY_POWER_STATE_CHARACTERISTIC_UUID,
            "Battery power",
            batteryPowerStateDataCallback
        )


    init {
        addCharacteristic(level)
        addCharacteristic(power)
    }


}
