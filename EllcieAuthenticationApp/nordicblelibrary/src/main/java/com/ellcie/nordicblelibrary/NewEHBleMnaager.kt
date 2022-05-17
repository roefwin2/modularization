package com.ellcie.nordicblelibrary

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import androidx.annotation.NonNull
import androidx.lifecycle.asFlow
import com.ellcie.nordicblelibrary.profile.BatteryService
import com.ellcie.nordicblelibrary.profile.NordicBleService
import com.ellcie_healthy.ble_library.ble.profile.BleService
import com.ellcie_healthy.ble_library.ble.profile.BleWriteCharacteristic
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import no.nordicsemi.android.ble.WriteRequest
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.ble.livedata.ObservableBleManager
import no.nordicsemi.android.ble.livedata.state.ConnectionState
import java.lang.Exception
import java.util.*

class NewEHBleManager(context: Context) : ObservableBleManager(context) {

    private val services: MutableList<NordicBleService<*>> = mutableListOf()
    private val batteryService : BatteryService = BatteryService(UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb"),this)

    val state: Flow<ConnectionState> get() = state.asFlow()

    val level get() = batteryService.levelState


    init {
        services.add(batteryService)
    }


    override fun getGattCallback(): BleManagerGattCallback {

    }

    fun connect() {
        bluetoothDevice?.let { connect(it) }
    }


    fun disconnectDevice() {
        try {
            disconnect().await()
        } catch (e: Exception) {

        }
    }

    fun write(bleWriteCharacteristic: BleWriteCharacteristic, data: Data) {
        try {
            writeCharacteristic(
                bleWriteCharacteristic.characteristic,
                data,
                BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            ).await()
        } catch (e: Exception) {

        }
    }

    fun read(bleWriteCharacteristic: BleWriteCharacteristic) {
        readCharacteristic(bleWriteCharacteristic.characteristic).await()
    }

    private inner class NewBleManagerGattCallback : BleManagerGattCallback() {

        override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
        }

        override fun onServicesInvalidated() {
        }

        override fun initialize() {
            super.initialize()
            services.forEach {
                it.initialize()
            }
        }

    }
}
