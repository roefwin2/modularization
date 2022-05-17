package com.ellcie_healthy.ble_library.ble.profile.generic.access.callback;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

import com.ellcie_healthy.ble_library.ble.profile.ReadStatusCallback;
import com.ellcie_healthy.ble_library.ble.profile.generic.access.data.BleDeviceAppearance;

import no.nordicsemi.android.ble.callback.profile.ProfileDataCallback;
import no.nordicsemi.android.ble.data.Data;

public abstract class AppearanceDataCallback implements ProfileDataCallback, AppearanceCallback, ReadStatusCallback {
    @Override
    public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
        if (data.size() != 2) {
            onInvalidDataReceived(device, data);
            return;
        }
        final int intAppearance = data.getIntValue(Data.FORMAT_UINT16, 0);
        onAppearance(device, BleDeviceAppearance.valueOf(intAppearance));
    }
}
