package com.ellcie_healthy.ble_library.ble.profile.command.data;

import androidx.annotation.NonNull;

public enum WarningCode {
    WARNING_TEMP_LOW(0x01),
    WARNING_TEMP_HIGH(0x02),
    WARNING_CHARGE_DISABLE_TEMP_LOW(0x03),
    WARNING_CHARGE_DISABLE_TEMP_HIGH(0x04),
    WARNING_TEMP_OK(0x05);

    private final int code;

    WarningCode(int c) {
        code = c;
    }

    public static WarningCode valueOf(int value) {
        for (WarningCode e : values()) {
            if ((byte) e.code == (byte) value) {
                return e;
            }
        }
        return null;
    }

    @NonNull
    @Override
    public String toString() {
        return super.toString() + " (" + String.format("0x%02X", (byte) code) + ")";
    }
}
