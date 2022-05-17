package com.ellcie_healthy.ble_library.ble.profile.generic.access.callback;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

import com.ellcie_healthy.ble_library.ble.profile.generic.access.data.BleDeviceAppearance;

public interface AppearanceCallback {
    void onAppearance(@NonNull final BluetoothDevice device, @NonNull final BleDeviceAppearance appearance);
}
