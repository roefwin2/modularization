package com.ellcie_healthy.ble_library.ble.profile.generic.information.callback;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

public interface SerialNumberCallback {
    void onSerialNumber(@NonNull final BluetoothDevice device, @NonNull final String serial);
}
