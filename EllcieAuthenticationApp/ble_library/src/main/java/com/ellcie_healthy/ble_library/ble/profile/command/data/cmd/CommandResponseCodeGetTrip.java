package com.ellcie_healthy.ble_library.ble.profile.command.data.cmd;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public enum CommandResponseCodeGetTrip {
    TRIP_RUNNING(0x0),
    TRIP_STOPPED(0x1),
    TRIP_CALIBRATION(0x2),
    TRIP_BSQ(0x3);

    private final int code;

    CommandResponseCodeGetTrip(int c) {
        code = c;
    }

    @Nullable
    public static CommandResponseCodeGetTrip valueOf(int value) {
        for (CommandResponseCodeGetTrip e : values()) {
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
