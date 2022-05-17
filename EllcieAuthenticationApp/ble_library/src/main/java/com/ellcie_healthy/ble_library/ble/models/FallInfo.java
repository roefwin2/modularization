package com.ellcie_healthy.ble_library.ble.models;

import com.ellcie_healthy.ble_library.ble.profile.command.data.FallState;

public class FallInfo {
    private FallState mState;
    private long mTimestamp;

    public FallInfo() {
        clear();
    }

    public void clear() {
        mState = FallState.FALL_RECOVERY;
        mTimestamp = -1;
    }

    public FallState getState() {
        return mState;
    }

    public void setState(FallState mState) {
        this.mState = mState;
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public void setTimestamp(long mTimestamp) {
        this.mTimestamp = mTimestamp;
    }
}
