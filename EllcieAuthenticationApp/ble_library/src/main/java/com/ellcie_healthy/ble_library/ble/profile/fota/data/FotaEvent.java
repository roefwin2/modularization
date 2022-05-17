package com.ellcie_healthy.ble_library.ble.profile.fota.data;

import androidx.annotation.NonNull;

public class FotaEvent {
    private final FotaEventType mType;
    private final int mCounter;

    public FotaEvent(@NonNull final FotaEventType type, final int counter) {
        mType = type;
        mCounter = counter;
    }

    public final FotaEventType getType() {
        return mType;
    }

    public final int getCounter() {
        return mCounter;
    }

    @NonNull
    @Override
    public String toString() {
        return mCounter + " - " + mType;
    }

    public enum FotaEventType {
        FOTA_SUCCESS(0x0000),
        FOTA_FLASH_VERIFY_ERROR(0x003C),
        FOTA_FLASH_WRITE_ERROR(0x00FF),
        FOTA_SEQUENCE_ERROR(0x00F0),
        FOTA_CHECKSUM_ERROR(0x000F),
        FOTA_EVENT_UNKNOWN(0xFFFF);

        private final int code;

        FotaEventType(int c) {
            code = c;
        }

        public static FotaEventType valueOf(int value) {
            for (FotaEventType e : values()) {
                if (e.code == value) {
                    return e;
                }
            }
            return FOTA_EVENT_UNKNOWN;
        }

        @NonNull
        @Override
        public String toString() {
            return super.toString() + " (" + String.format("0x%04X", code) + ")";
        }

    }
}
