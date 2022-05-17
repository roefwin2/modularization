package com.ellcie_healthy.ble_library.ble.profile.fota.callback;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

import com.ellcie_healthy.ble_library.ble.profile.ReadStatusCallback;
import com.ellcie_healthy.ble_library.ble.profile.fota.data.ImageData;

import org.jetbrains.annotations.NotNull;

import java.security.InvalidParameterException;

import no.nordicsemi.android.ble.callback.profile.ProfileDataCallback;
import no.nordicsemi.android.ble.data.Data;

public abstract class ImageDataCallback implements ProfileDataCallback, ImageCallback, ReadStatusCallback {
    @Override
    public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
        if (data.size() != 8) {
            onInvalidDataReceived(device, data);
            return;
        }

        try {
            final ImageData image = new ImageData(data.getIntValue(Data.FORMAT_UINT32, 0), data.getIntValue(Data.FORMAT_UINT32, 4));
            onFotaImage(device, image);
        } catch (InvalidParameterException exception) {
            onInvalidDataReceived(device, data);
        }
    }

    @Override
    public void onRequestFailed(@NonNull @NotNull BluetoothDevice device, int status) {
        onFotaImage(device, null);
    }
}
