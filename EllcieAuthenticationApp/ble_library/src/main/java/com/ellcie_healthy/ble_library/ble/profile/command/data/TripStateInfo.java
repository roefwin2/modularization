package com.ellcie_healthy.ble_library.ble.profile.command.data;

import androidx.annotation.NonNull;

public enum TripStateInfo {
    NONE(0xFF),
    STOP_CAUSE_TAP(0x00),
    STOP_CAUSE_CHARGER(0x01),
    STOP_CAUSE_PAIRING(0x02),
    STOP_CAUSE_OTA(0x03),
    STOP_CAUSE_DIAG(0x04),
    STOP_CAUSE_CALIB_TIMEOUT(0xA0),
    STOP_CAUSE_NO_BLINK(0xA2),
    STOP_CAUSE_SENSOR_ERR(0xA3),
    STOP_CAUSE_GLASSES_REMOVED(0xA4),
    STOP_CAUSE_MAKEUP(0xA5),
    STOP_CAUSE_TOO_DROWSY(0xA6),
    WARNING_NO_BLINK(0xB1),
    WARNING_GLASSES_REMOVED(0xB2),
    WARNING_MAKEUP(0xB3),
    SIGNAL_RESTORED(0xF0),
    CALIBRATION_DONE(0xF1);

    private final int code;

    TripStateInfo(int c) {
        code = c;
    }

    public static TripStateInfo valueOf(int value) {
        for (TripStateInfo e : values()) {
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

    public int getCode() {
        return code;
    }
}
