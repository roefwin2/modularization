package com.ellcie_healthy.ble_library.ble.profile.command.data.event;

import androidx.annotation.NonNull;

public enum EventCode {
    TRIP_STATE_CHANGE(0x01),
    TAPS_MODE(0x02),
    LOCALIZE_MY_PHONE(0xA5),
    WARNING(0xF0),
    HARDWARE_FAULT(0xF1),
    SOFTWARE_FAULT(0xF2),
    FALL_EVENT(0xFA),
    REBOOT_INIIATED(0xFE),
    SHUTDOWN_INIIATED(0xFF);

    private final int code;

    EventCode(int c) {
        code = c;
    }

    public static EventCode valueOf(int value) {
        for (EventCode e : values()) {
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
