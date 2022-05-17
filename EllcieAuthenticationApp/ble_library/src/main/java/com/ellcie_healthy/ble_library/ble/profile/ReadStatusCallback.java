package com.ellcie_healthy.ble_library.ble.profile;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import androidx.annotation.NonNull;

import no.nordicsemi.android.ble.callback.DataReceivedCallback;
import no.nordicsemi.android.ble.callback.FailCallback;

public interface ReadStatusCallback extends DataReceivedCallback, FailCallback {
    @Override
    default void onRequestFailed(@NonNull BluetoothDevice device, int status) {
        Log.e("ReadStatusCallback", "default onRequestFailed: " + status);
    }
}