package com.ellcie_healthy.ble_library.ble.profile.fota.callback;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

import com.ellcie_healthy.ble_library.ble.profile.fota.data.FotaEvent;

public interface FotaEventCallback {
    void onFotaEvent(@NonNull final BluetoothDevice device, @NonNull final FotaEvent event);
}
