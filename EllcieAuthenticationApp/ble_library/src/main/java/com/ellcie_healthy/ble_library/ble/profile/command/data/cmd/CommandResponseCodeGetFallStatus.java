package com.ellcie_healthy.ble_library.ble.profile.command.data.cmd;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public enum CommandResponseCodeGetFallStatus {
    NO_EVENT(0x0),
    FALL_DETECTION_ENGAGED(0x1),
    FALL_CONFIRMED_FROM_GLASSES(0x2),
    FALL_CONFIRMED_FROM_MOBILE(0x3),
    SOS_ENGAGED_FROM_GLASSES(0x9),
    SOS_ENGAGED_FROM_MOBILE(0xA),
    SOS_CONFIRMED_FROM_GLASSES(0xB),
    SOS_CONFIRMED_FROM_MOBILE(0xC);

    private final int code;

    CommandResponseCodeGetFallStatus(int c) {
        code = c;
    }

    @Nullable
    public static CommandResponseCodeGetFallStatus valueOf(int value) {
        for (CommandResponseCodeGetFallStatus e : values()) {
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
