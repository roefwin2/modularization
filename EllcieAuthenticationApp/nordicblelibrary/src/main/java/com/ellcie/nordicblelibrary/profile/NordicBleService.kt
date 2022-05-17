package com.ellcie.nordicblelibrary.profile

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattService
import com.ellcie.nordicblelibrary.NewEHBleManager
import com.ellcie_healthy.ble_library.ble.profile.BleCharacteristic
import com.ellcie_healthy.ble_library.ble.profile.ReadStatusCallback
import java.util.*

abstract class NordicBleService<E>(
     private val newEHBleManager: NewEHBleManager,
     private val uuid: UUID,
     private val name : String,
     private val characteristics : MutableList<NordicBleCharacteristic<*>> = mutableListOf(),
     private var bluetoothGattService: BluetoothGattService? = null

 ) {

    protected fun addCharacteristic(characteristic: NordicBleCharacteristic<*>){
        characteristics.add(characteristic)
        bluetoothGattService?.let { characteristic.setSupportedCharacteristics(it) }
    }

    protected fun isRequiredServiceSupported(gatt: BluetoothGatt){
        bluetoothGattService = gatt.getService(uuid)
    }

    fun initialize() {
        newEHBleManager
    }

}