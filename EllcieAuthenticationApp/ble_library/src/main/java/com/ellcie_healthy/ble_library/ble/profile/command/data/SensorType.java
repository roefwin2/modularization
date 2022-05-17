package com.ellcie_healthy.ble_library.ble.profile.command.data;

import androidx.annotation.NonNull;

public enum SensorType {
    ANY(0x00),

    PEDOMETER(0x0A),
    EYE_SENSOR_RIGHT_DOWN(0x0B, SensorFamily.EYE_SENSOR),
    EYE_SENSOR_LEFT_DOWN(0x0C, SensorFamily.EYE_SENSOR),
    EYE_SENSOR_LEFT_RIGHT(0x0D, SensorFamily.EYE_SENSOR),
    EYE_SENSOR_RIGHT_UP(0x0E, SensorFamily.EYE_SENSOR),
    EYE_SENSOR_LEFT_UP(0x0F, SensorFamily.EYE_SENSOR),
    TEMPERATURE(0x1A),
    HUMIDITY(0x1B),
    ATMO_PRESSURE(0x1C),
    AMB_LIGHT(0x1D),
    BATT_TEMP(0x2A),
    ACCELEROMETER(0x30),
    GYROSCOPE(0x31),
    ACC_GYRO(0x32),
    BLINK_DROWSINESS_INFO(0x33),
    LOOK_DIRECTION(0x34),
    HEAD_ROTATION(0x35),
    OPENLAB_RAW(0x50),
    OPENLAB_COMPUTED(0x51),

    DISABLE(0xFF);

    private final int code;
    private final SensorFamily family;

    SensorType(int c) {
        this(c, SensorFamily.NONE);
    }

    SensorType(int c, @NonNull SensorFamily f) {
        code = c;
        family = f;
    }

    public static SensorType valueOf(int value) {
        for (SensorType e : values()) {
            if (e != SensorType.DISABLE && (byte) e.code == (byte) value) {
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

    public final SensorFamily getFamily() {
        return family;
    }
}
