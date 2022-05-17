package com.ellcie_healthy.ble_library.ble.profile.generic.information.callback;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

public interface FirmwareRevisionCallback {
    void onFirmwareRevision(@NonNull final BluetoothDevice device, @NonNull final String firmware);
}
