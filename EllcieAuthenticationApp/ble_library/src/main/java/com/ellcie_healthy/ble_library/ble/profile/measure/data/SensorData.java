package com.ellcie_healthy.ble_library.ble.profile.measure.data;

import androidx.annotation.NonNull;

import com.ellcie_healthy.ble_library.ble.profile.command.data.SensorType;

public class SensorData<T> {
    public static final String DATA_SEPARATOR = ";";
    private final SensorType mSensorType;
    protected long mTimestamp;
    protected T mData;

    public SensorData(@NonNull SensorType type, @NonNull long timestamp, @NonNull final T data) {
        mSensorType = type;
        this.mTimestamp = timestamp;
        this.mData = data;
    }

    public SensorData(@NonNull SensorType type, @NonNull long timestamp) {
        mSensorType = type;
        this.mTimestamp = timestamp;
    }

    @NonNull
    public final SensorType getSensorType() {
        return mSensorType;
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public T getData() {
        return mData;
    }

    @NonNull
    @Override
    public String toString() {
        return mTimestamp + DATA_SEPARATOR + mData;
    }
}
