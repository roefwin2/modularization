package com.ellcie_healthy.ble_library.ble.profile.fota.data;

import java.security.InvalidParameterException;

public class ImageData {
    private final int mStartAddress;
    private final int mEndAddress;
    private final int mSizeAvailable;
    private final ImageSlot mSlot;

    public ImageData(final int startAddress, final int endAddress) throws InvalidParameterException {
        mSlot = ImageSlot.valueOf(startAddress);
        if (mSlot == null)
            throw new InvalidParameterException("Invalid slot start address: " + startAddress);

        mStartAddress = startAddress;
        mEndAddress = endAddress;
        mSizeAvailable = endAddress - startAddress;
    }

    public final int getSizeAvailable() {
        return mSizeAvailable;
    }

    public final ImageSlot getSlot() {
        return mSlot;
    }
}
