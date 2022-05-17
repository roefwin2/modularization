package com.ellcie_healthy.ble_library.ble.profile.generic.access.callback;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

import com.ellcie_healthy.ble_library.ble.profile.ReadStatusCallback;

import no.nordicsemi.android.ble.callback.profile.ProfileDataCallback;
import no.nordicsemi.android.ble.data.Data;

public abstract class DeviceNameDataCallback implements ProfileDataCallback, DeviceNameCallback, ReadStatusCallback {
    @Override
    public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
        final String name = data.getStringValue(0);

        onDeviceName(device, name == null ? "" : name);
    }
}
