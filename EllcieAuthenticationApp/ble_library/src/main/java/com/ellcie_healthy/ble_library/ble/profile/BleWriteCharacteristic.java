package com.ellcie_healthy.ble_library.ble.profile;

import android.bluetooth.BluetoothGattCharacteristic;

import java.util.UUID;

public abstract class BleWriteCharacteristic extends BleCharacteristic<SendReceiveCallback> {
    public BleWriteCharacteristic(UUID uuid, String name) {
        super(uuid, name, false, BluetoothGattCharacteristic.PROPERTY_WRITE);
    }

    public BleWriteCharacteristic(UUID uuid, String name, int wantedProperties) {
        super(uuid, name, false, BluetoothGattCharacteristic.PROPERTY_WRITE | wantedProperties);
    }
}
