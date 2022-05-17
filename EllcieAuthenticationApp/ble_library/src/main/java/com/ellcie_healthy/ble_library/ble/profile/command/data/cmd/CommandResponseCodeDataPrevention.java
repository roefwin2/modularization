package com.ellcie_healthy.ble_library.ble.profile.command.data.cmd;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public enum CommandResponseCodeDataPrevention {
    OK(0x0),
    NO_EVENT(0x1),
    NOT_LOADED(0xE0),
    ALREADY_STARTED(0xE1),
    NOT_RUNNING(0xE2),
    INVALID_POSITION(0xE3);

    private final int code;

    CommandResponseCodeDataPrevention(int c) {
        code = c;
    }

    @Nullable
    public static CommandResponseCodeDataPrevention valueOf(int value) {
        for (CommandResponseCodeDataPrevention e : values()) {
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
