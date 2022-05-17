package com.ellcie_healthy.ble_library.ble.profile.command.data;

import androidx.annotation.NonNull;

public enum FallState {
    NO_EVENT(0x00),
    FALL_ENGAGED(0x01),
    FALL_CONFIRMED(0x02),
    FALL_CONFIRMED_APP(0x03),
    FALL_RECOVERY(0x04),
    FALL_CANCELLED(0x05),
    SOS_ENGAGED(0x09),
    SOS_ENGAGED_FROM_MOBILE_APP(0x0A),
    SOS_CONFIRMED(0x0B),
    SOS_CONFIRMED_FROM_MOBILE_APP(0x0B),
    SOS_CANCELLED(0x0D),
    STREAMING_STOPPED(0x50),
    STREAMING_STARTING(0x51);

    private final int code;

    FallState(int c) {
        code = c;
    }

    public static FallState valueOf(int value) {
        for (FallState e : values()) {
            if ((byte) e.code == (byte) value) {
                return e;
            }
        }
        return null;
    }

    public int getCode() {
        return code;
    }

    @NonNull
    @Override
    public String toString() {
        return super.toString() + " (" + String.format("0x%02X", (byte) code) + ")";
    }
}
