package com.ellcie_healthy.ble_library.ble.profile.measure.callback;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

public interface PedometerCallback {
    void onStepValue(@NonNull final BluetoothDevice device, @NonNull final int steps);
}
