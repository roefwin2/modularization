package com.ellcie_healthy.ble_library.ble.profile.measure.callback;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

import com.ellcie_healthy.ble_library.ble.profile.ReadStatusCallback;
import com.ellcie_healthy.ble_library.ble.profile.command.data.SensorType;

import no.nordicsemi.android.ble.callback.profile.ProfileDataCallback;
import no.nordicsemi.android.ble.data.Data;

public abstract class StreamingDataCallback implements ProfileDataCallback, StreamingCallback, ReadStatusCallback {
    @Override
    public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
        if (data.size() != 20) {
            onInvalidDataReceived(device, data);
            return;
        }

        SensorType sensor = SensorType.valueOf(data.getByte(0));
        if (sensor == null) {
            onInvalidDataReceived(device, data);
            return;
        }

        onStreamingData(device, sensor, data);
    }
}
