package com.ellcie_healthy.ble_library.ble.profile.generic.information.callback;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

import com.ellcie_healthy.ble_library.ble.profile.ReadStatusCallback;

import no.nordicsemi.android.ble.callback.profile.ProfileDataCallback;
import no.nordicsemi.android.ble.data.Data;

public abstract class ModelNumberDataCallback implements ProfileDataCallback, ModelNumberCallback, ReadStatusCallback {
    private static final int MODEL_NUMBER_SIZE = 8;

    @Override
    public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
        if (data.size() != MODEL_NUMBER_SIZE) {
            onInvalidDataReceived(device, data);
            return;
        }

        final String model = data.getStringValue(0);

        if (model == null || model.length() != MODEL_NUMBER_SIZE) {
            onInvalidDataReceived(device, data);
            return;
        }

        onModelNumber(device, model);
    }
}
