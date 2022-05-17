package com.ellcie_healthy.ble_library.ble.models;

import androidx.annotation.Nullable;

public class SensorsInfo {
    private Integer mHumidity = null;
    private Integer mPressure = null;
    private Integer mTemperature = null;
    private int mSteps;
    private int mWornDuration;

    public void clear() {
        mHumidity = null;
        mPressure = null;
        mTemperature = null;
        mSteps = 0;
    }

    @Nullable
    public Integer getHumidity() {
        return mHumidity;
    }

    public void setHumidity(Integer mHumidity) {
        this.mHumidity = mHumidity;
    }

    @Nullable
    public Integer getPressure() {
        return mPressure;
    }

    public void setPressure(Integer mPressure) {
        this.mPressure = mPressure;
    }

    @Nullable
    public Integer getTemperature() {
        return mTemperature;
    }

    public void setTemperature(Integer mTemperature) {
        this.mTemperature = mTemperature;
    }

    public int getSteps() {
        return mSteps;
    }

    public void setSteps(int mSteps) {
        this.mSteps = mSteps;
    }

    public int getWornDuration() {
        return mWornDuration;
    }

    public void setWornDuration(int duration) {
        this.mWornDuration = duration;
    }
}
