package com.ellcie_healthy.ble_library.ble.profile.command.data;

import androidx.annotation.NonNull;

public enum SilentModeReason {
    SILENT_MODE_ENABLED(0x01);

    private final int code;

    SilentModeReason(int c) {
        code = c;
    }

    public static SilentModeReason valueOf(int value) {
        for (SilentModeReason e : values()) {
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
