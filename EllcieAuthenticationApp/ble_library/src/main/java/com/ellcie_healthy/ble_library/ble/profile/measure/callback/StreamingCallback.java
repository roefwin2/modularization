package com.ellcie_healthy.ble_library.ble.profile.measure.callback;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

import com.ellcie_healthy.ble_library.ble.profile.command.data.SensorType;

import no.nordicsemi.android.ble.data.Data;

public interface StreamingCallback {
    void onStreamingData(@NonNull final BluetoothDevice device, @NonNull SensorType sensor, @NonNull final Data data);
}
