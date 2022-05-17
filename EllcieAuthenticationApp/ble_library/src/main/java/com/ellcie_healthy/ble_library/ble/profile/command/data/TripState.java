package com.ellcie_healthy.ble_library.ble.profile.command.data;

import androidx.annotation.NonNull;

public enum TripState {
    STOPPED(0x00),
    RUNNING(0x01),
    STARTED_CALIBRATION(0x02),
    BSQ(0x03);

    private final int code;

    TripState(int c) {
        code = c;
    }

    public static TripState valueOf(int value) {
        for (TripState e : values()) {
            if ((byte) e.code == (byte) value) {
                return e;
            }
        }
        return null;
    }

    @NonNull
    @Override
    public String toString() {
        return super.toString() + " (" + String.format("0x%02X", code) + ")";
    }
}
