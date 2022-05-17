package com.ellcie_healthy.ble_library.ble.profile.measure.data;

import androidx.annotation.NonNull;

public class HeadRotationData {
    private float pitch, roll, yaw;

    public HeadRotationData(@NonNull float pitch, @NonNull float roll, @NonNull float yaw) {
        this.pitch = pitch;
        this.roll = roll;
        this.yaw = yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public float getRoll() {
        return roll;
    }

    public float getYaw() {
        return yaw;
    }

    @Override
    public String toString() {
        return pitch + SensorData.DATA_SEPARATOR + roll + SensorData.DATA_SEPARATOR + yaw;
    }
}
