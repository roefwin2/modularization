package com.ellcie_healthy.ble_library.ble.models;

import androidx.annotation.NonNull;

import com.ellcie_healthy.ble_library.ble.profile.command.data.TripState;
import com.ellcie_healthy.ble_library.ble.profile.command.data.TripStateInfo;
import com.ellcie_healthy.ble_library.ble.profile.measure.data.RiskData;

public class TripInfo {
    private TripStateInfo mInfo;
    private int mId;
    private long mStartTsMs;
    private long mDuration;
    private String mDriverTripId;
    private TripState mState;

    private RiskData.RiskLevel mRiskLevel;

    public TripInfo() {
        clear();
    }

    public void clear() {
        mState = TripState.STOPPED;
        mInfo = TripStateInfo.NONE;
        mId = -1;
        mDriverTripId = "";
        mStartTsMs = -1;
        mDuration = -1;
        mRiskLevel = RiskData.RiskLevel.RISK_LEVEL_1;
    }

    public TripStateInfo getInfo() {
        return mInfo;
    }

    public void setInfo(TripStateInfo mInfo) {
        this.mInfo = mInfo;
    }

    public int getId() {
        return mId;
    }

    public void setId(int mId) {
        this.mId = mId;
    }

    public void setDriverTripId(@NonNull String tripId){
        this.mDriverTripId = tripId;
    }

    @NonNull
    public String getDriverTripId() {
        return mDriverTripId;
    }

    public long getStartTsMs() {
        return mStartTsMs;
    }

    public void setStartTsMs(long mStartTsMs) {
        this.mStartTsMs = mStartTsMs;
    }

    public long getDuration() {
        return mDuration;
    }

    public void setDuration(long mDuration) {
        this.mDuration = mDuration;
    }

    public TripState getState() {
        return mState;
    }

    public void setState(TripState mState) {
        this.mState = mState;
    }

    public RiskData.RiskLevel getRiskLevel() {
        return mRiskLevel;
    }

    public void setRiskLevel(RiskData.RiskLevel risk) {
        mRiskLevel = risk;
    }
}
