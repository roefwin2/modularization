package com.ellcie.nordicblelibrary.profile

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import com.ellcie_healthy.ble_library.ble.profile.ReadStatusCallback
import java.util.*

open class NordicBleCharacteristic<E : ReadStatusCallback>(
    private val uuid: UUID,
    private val name : String,
    protected val callback : E
){

    lateinit var characteristic: BluetoothGattCharacteristic

    fun setSupportedCharacteristics(bluetoothGattService: BluetoothGattService){
        characteristic = bluetoothGattService.getCharacteristic(uuid)
    }
}

