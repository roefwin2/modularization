package com.ellcie_healthy.ble_library.ble.profile.generic.access.callback;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

public interface DeviceNameCallback {
    void onDeviceName(@NonNull final BluetoothDevice device, @NonNull final String name);
}
