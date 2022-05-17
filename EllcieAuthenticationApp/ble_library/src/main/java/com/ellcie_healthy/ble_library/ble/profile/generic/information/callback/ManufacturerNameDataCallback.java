package com.ellcie_healthy.ble_library.ble.profile.generic.information.callback;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

import com.ellcie_healthy.ble_library.ble.profile.ReadStatusCallback;

import no.nordicsemi.android.ble.callback.profile.ProfileDataCallback;
import no.nordicsemi.android.ble.data.Data;

public abstract class ManufacturerNameDataCallback implements ProfileDataCallback, ManufacturerNameCallback, ReadStatusCallback {
    private static final int MANUFACTURER_SIZE = 14;

    @Override
    public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
        if (data.size() != MANUFACTURER_SIZE) {
            onInvalidDataReceived(device, data);
            return;
        }

        final String manufacturer = data.getStringValue(0);

        if (manufacturer == null || manufacturer.length() != MANUFACTURER_SIZE) {
            onInvalidDataReceived(device, data);
            return;
        }

        onManufacturerName(device, manufacturer);
    }
}
