package com.ellcie_healthy.ble_library.ble.profile.measure.callback;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

import com.ellcie_healthy.ble_library.ble.profile.ReadStatusCallback;

import no.nordicsemi.android.ble.callback.profile.ProfileDataCallback;
import no.nordicsemi.android.ble.data.Data;

public abstract class DataGatheringDataCallback implements ProfileDataCallback, DataGatheringCallback, ReadStatusCallback {
    @Override
    public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
        if (data.size() != 20) {
            onInvalidDataReceived(device, data);
            return;
        }

        onDataGatheringData(device, data);
    }
}
