package com.ellcie_healthy.ble_library.ble.profile.fota.data;

import androidx.annotation.NonNull;

public enum ImageSlot {
    SLOT_0(0x08018000),
    SLOT_1(0x0804c000);

    private final int mStartAddress;

    ImageSlot(int startAddress) {
        mStartAddress = startAddress;
    }

    public static ImageSlot valueOf(int startAddress) {
        for (ImageSlot e : values()) {
            if (e.mStartAddress == startAddress) {
                return e;
            }
        }
        return null;
    }

    @NonNull
    @Override
    public String toString() {
        return super.toString() + " (" + String.format("0x%08X", mStartAddress) + ")";
    }

    public final int getSlotAddress() {
        return mStartAddress;
    }
}
