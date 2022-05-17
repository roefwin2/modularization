package com.ellcie_healthy.ble_library.ble.profile.measure.callback;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

import com.ellcie_healthy.ble_library.ble.profile.measure.data.RiskData;

public interface RiskCallback {
    void onRiskValue(@NonNull final BluetoothDevice device, @NonNull final RiskData risk);
}
