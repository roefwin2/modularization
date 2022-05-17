package com.ellcie_healthy.ble_library.ble.models.streaming;

import androidx.annotation.NonNull;

import com.ellcie_healthy.ble_library.ble.profile.measure.data.SensorData;

public interface SensorDataCallback<T> {
    void onSensorData(@NonNull SensorData<T> data);
}
