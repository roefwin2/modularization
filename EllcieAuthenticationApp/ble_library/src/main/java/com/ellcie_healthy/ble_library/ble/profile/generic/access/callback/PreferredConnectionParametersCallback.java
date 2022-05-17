package com.ellcie_healthy.ble_library.ble.profile.generic.access.callback;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

import no.nordicsemi.android.ble.data.Data;

public interface PreferredConnectionParametersCallback {
    void onPreferedConnectionParameters(@NonNull final BluetoothDevice device, @NonNull Data data);
}
