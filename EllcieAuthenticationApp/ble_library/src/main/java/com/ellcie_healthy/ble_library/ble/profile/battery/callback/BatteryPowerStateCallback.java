package com.ellcie_healthy.ble_library.ble.profile.battery.callback;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

public interface BatteryPowerStateCallback {
    void onBatteryPowerState(@NonNull final BluetoothDevice device, final Boolean plug, final Boolean charging);
}
