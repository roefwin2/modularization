package com.ellcie_healthy.ble_library.ble.profile.generic.information.callback;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

import com.ellcie_healthy.ble_library.ble.profile.ReadStatusCallback;

import no.nordicsemi.android.ble.callback.profile.ProfileDataCallback;
import no.nordicsemi.android.ble.data.Data;

public abstract class SerialNumberDataCallback implements ProfileDataCallback, SerialNumberCallback, ReadStatusCallback {
    private static final int SERIAL_NUMBER_SIZE = 16;

    @Override
    public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
        if (data.size() != SERIAL_NUMBER_SIZE) {
            onInvalidDataReceived(device, data);
            return;
        }

        final String serial = data.getStringValue(0);

        if (serial == null || serial.length() != SERIAL_NUMBER_SIZE) {
            onInvalidDataReceived(device, data);
            return;
        }

        onSerialNumber(device, serial);
    }
}
