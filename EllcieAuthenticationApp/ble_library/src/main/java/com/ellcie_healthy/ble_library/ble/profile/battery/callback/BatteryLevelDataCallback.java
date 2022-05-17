package com.ellcie_healthy.ble_library.ble.profile.battery.callback;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

import com.ellcie_healthy.ble_library.ble.profile.ReadStatusCallback;

import no.nordicsemi.android.ble.callback.profile.ProfileDataCallback;
import no.nordicsemi.android.ble.data.Data;

public abstract class BatteryLevelDataCallback implements ProfileDataCallback, BatteryLevelCallback, ReadStatusCallback {
    @Override
    public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
        if (data.size() != 1) {
            onInvalidDataReceived(device, data);
            return;
        }
        final int batteryLevel = data.getIntValue(Data.FORMAT_UINT8, 0);
        if (batteryLevel < 0 || batteryLevel > 100) {
            onInvalidDataReceived(device, data);
            return;
        }

        onBatteryLevel(device, batteryLevel);
    }
}