package com.ellcie_healthy.ble_library.ble.profile;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

public interface UnexpectedErrorCallback {
    void onUnexpectedError(@NonNull BluetoothDevice device, int status, @NonNull String message);
}
