package com.ellcie_healthy.ble_library.ble.profile.command.data;

import androidx.annotation.NonNull;

public enum ShutdownReason {
    LOW_BATTERY(0x00),
    TEMP_LOW(0x01),
    TEMP_HIGH(0x02);

    private final int code;

    ShutdownReason(int c) {
        code = c;
    }

    public static ShutdownReason valueOf(int value) {
        for (ShutdownReason e : values()) {
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
