package com.ellcie_healthy.ble_library.ble.profile.command.data;

import androidx.annotation.NonNull;

public enum TripEventState {
    NOISE_CALIB_DONE(0x00),
    NOISE_CALIB_ERROR(0x01),
    SIMPLE_BLINK_CALIB_DONE(0x02),
    LONG_BLINK_CALIB_DONE(0x03),
    SIMPLE_BLINK_CALIB_ERROR(0x04),
    LONG_BLINK_CALIB_ERROR(0x05),
    NO_SIMPLE_BLINK_ERROR(0x06),
    NO_LONG_BLINK_ERROR(0x07),
    DROWSINESS_ALARM(0xF0);

    private final int code;

    TripEventState(int c) {
        code = c;
    }

    public static TripEventState valueOf(int value) {
        for (TripEventState e : values()) {
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

    public final int getCode() {
        return code;
    }
}
