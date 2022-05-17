package com.ellcie_healthy.ble_library.ble.profile.generic.attribute.callback;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

import no.nordicsemi.android.ble.data.Data;

public interface ServiceChangedCallback {
    void onServiceChanged(@NonNull final BluetoothDevice device, @NonNull final Data data);
}
