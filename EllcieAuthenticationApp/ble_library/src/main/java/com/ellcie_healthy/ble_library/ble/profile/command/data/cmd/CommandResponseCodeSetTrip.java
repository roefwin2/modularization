package com.ellcie_healthy.ble_library.ble.profile.command.data.cmd;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public enum CommandResponseCodeSetTrip {
    OK(0x0),
    ALREADY_RUNNING(0x1),
    ALREADY_STOPPED(0x2),
    IN_CHARGE(0x3);

    private final int code;

    CommandResponseCodeSetTrip(int c) {
        code = c;
    }

    @Nullable
    public static CommandResponseCodeSetTrip valueOf(int value) {
        for (CommandResponseCodeSetTrip e : values()) {
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
