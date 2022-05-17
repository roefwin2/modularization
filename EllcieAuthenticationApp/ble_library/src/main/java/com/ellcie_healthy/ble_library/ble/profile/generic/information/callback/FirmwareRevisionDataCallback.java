package com.ellcie_healthy.ble_library.ble.profile.generic.information.callback;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

import com.ellcie_healthy.ble_library.ble.profile.ReadStatusCallback;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import no.nordicsemi.android.ble.callback.profile.ProfileDataCallback;
import no.nordicsemi.android.ble.data.Data;

public abstract class FirmwareRevisionDataCallback implements ProfileDataCallback, FirmwareRevisionCallback, ReadStatusCallback {
    private static final int FIRMWARE_MAX_SIZE = 16;
    private static final int FIRMWARE_MIN_SIZE = 5;

    @Override
    public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
        if (data.size() < FIRMWARE_MIN_SIZE || data.size() > FIRMWARE_MAX_SIZE) {
            onInvalidDataReceived(device, data);
            return;
        }

        final String version = data.getStringValue(0);

        if (version == null || version.length() < FIRMWARE_MIN_SIZE) {
            onInvalidDataReceived(device, data);
            return;
        }

        Pattern pattern = Pattern.compile("^\\d+\\.\\d+\\.\\d+(\\..+)?$");
        Matcher matcher = pattern.matcher(version);
        if (!matcher.find()) {
            onInvalidDataReceived(device, data);
            return;
        }

        onFirmwareRevision(device, version);
    }
}
