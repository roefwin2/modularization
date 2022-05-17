package com.ellcie_healthy.ble_library.ble.profile.measure.callback;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

import no.nordicsemi.android.ble.data.Data;

public interface DataGatheringCallback {
    void onDataGatheringData(@NonNull final BluetoothDevice device, @NonNull final Data data);
}
