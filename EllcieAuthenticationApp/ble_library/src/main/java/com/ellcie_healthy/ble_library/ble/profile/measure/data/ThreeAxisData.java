package com.ellcie_healthy.ble_library.ble.profile.measure.data;

import androidx.annotation.NonNull;

public class ThreeAxisData {
    private float x, y, z;

    public ThreeAxisData(@NonNull float x, @NonNull float y, @NonNull float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    @Override
    public String toString() {
        return x + SensorData.DATA_SEPARATOR + y + SensorData.DATA_SEPARATOR + z;
    }
}
