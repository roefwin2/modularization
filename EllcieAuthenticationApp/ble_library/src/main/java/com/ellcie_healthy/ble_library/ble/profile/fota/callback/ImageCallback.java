package com.ellcie_healthy.ble_library.ble.profile.fota.callback;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

import com.ellcie_healthy.ble_library.ble.profile.fota.data.ImageData;

public interface ImageCallback {
    void onFotaImage(@NonNull final BluetoothDevice device, @NonNull final ImageData image);
}
