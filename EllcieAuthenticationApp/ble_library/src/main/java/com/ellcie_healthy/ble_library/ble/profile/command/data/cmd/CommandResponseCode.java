package com.ellcie_healthy.ble_library.ble.profile.command.data.cmd;

import androidx.annotation.NonNull;

public enum CommandResponseCode {
    ERROR(-0xFF),
    CUSTOM_RESPONSE_CODE(-0xFE),

    OK(0x0),
    RESPONSE_ERROR_UNKNOWN_COMMAND(0xF0),
    RESPONSE_ERROR_BAD_ARGUMENTS(0xF1),
    RESPONSE_ERROR_NOT_IMPLEMENTED(0xF2),
    RESPONSE_ERROR_GENERIC(0xFF);

    private final int code;

    CommandResponseCode(int c) {
        code = c;
    }

    @NonNull
    public static CommandResponseCode valueOf(int value) {
        for (CommandResponseCode e : values()) {

            // if not valid byte value, skip them
            if (e.code >= 0 && (byte) e.code == (byte) value) {
                return e;
            }
        }

        if (value > 0 && value < RESPONSE_ERROR_UNKNOWN_COMMAND.getCode()) {
            return CUSTOM_RESPONSE_CODE;
        }
        return ERROR;
    }

    @NonNull
    @Override
    public String toString() {
        return super.toString() + " (" + String.format("0x%02X", code) + ")";
    }

    public final int getCode() {
        return code;
    }
}
