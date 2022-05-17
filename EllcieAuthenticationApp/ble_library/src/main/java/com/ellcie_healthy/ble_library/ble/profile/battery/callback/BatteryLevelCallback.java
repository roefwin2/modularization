package com.ellcie_healthy.ble_library.ble.profile.battery.callback;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

public interface BatteryLevelCallback {
    void onBatteryLevel(@NonNull final BluetoothDevice device,
                        @IntRange(from = 0, to = 100) final int batteryLevel);
}
