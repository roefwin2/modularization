package com.ellcie_healthy.ble_library.ble.profile.measure.callback;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

import com.ellcie_healthy.ble_library.ble.profile.ReadStatusCallback;
import com.ellcie_healthy.ble_library.ble.profile.measure.data.RiskData;

import no.nordicsemi.android.ble.callback.profile.ProfileDataCallback;
import no.nordicsemi.android.ble.data.Data;

public abstract class RiskDataCallback implements ProfileDataCallback, RiskCallback, ReadStatusCallback {
    @Override
    public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
        if (data.size() != 1 && data.size() != 3) {
            onInvalidDataReceived(device, data);
            return;
        }

        final int level = data.getIntValue(Data.FORMAT_UINT8, 0);
        final int tripId = data.size() > 1 ? data.getIntValue(Data.FORMAT_UINT16, 1) : 0;

        final RiskData risk = new RiskData(RiskData.RiskLevel.valueOf(level), tripId);
        onRiskValue(device, risk);
    }
}
