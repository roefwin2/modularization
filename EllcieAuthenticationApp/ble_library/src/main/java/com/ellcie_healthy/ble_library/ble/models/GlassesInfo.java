package com.ellcie_healthy.ble_library.ble.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class GlassesInfo {
    private String mSerial = null;
    private String mModel = null;
    private String mFirmware = null;

    public void clear() {
        mSerial = null;
        mModel = null;
        mFirmware = null;
    }

    @Nullable
    public String getSerial() {
        return mSerial;
    }

    public void setSerial(@NonNull String mSerial) {
        this.mSerial = mSerial;
    }

    @Nullable
    public String getModel() {
        return mModel;
    }

    public void setModel(@Nullable String mModel) {
        this.mModel = mModel;
    }

    @Nullable
    public String getFirmware() {
        return mFirmware;
    }

    public void setFirmware(@NonNull String mFirmware) {
        this.mFirmware = mFirmware;
    }
}
