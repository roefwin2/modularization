package com.ellcie_healthy.ble_library.ble.profile.command.data.cmd;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ellcie_healthy.ble_library.ble.profile.command.data.TripState;

import no.nordicsemi.android.ble.data.Data;

public class CommandGetTripStatus extends CommandResponse {
    private int mTripId = 0;
    private int mDuration = 0;
    private CommandResponseCodeGetTrip mStatus;
    private boolean mStreaming = false;

    public CommandGetTripStatus() {
        super(CommandCode.COMMAND_GET_TRIP_STATUS, 6, 7);
    }

    @Override
    protected boolean parseData(@NonNull final Data responseData) {
        mStatus = CommandResponseCodeGetTrip.valueOf(getIntResponseCode());

        if (mStatus == null) {
            return false;
        }

        mDuration = 0;
        mTripId = 0;

        if (mStatus != CommandResponseCodeGetTrip.TRIP_STOPPED) {
            mDuration = responseData.getIntValue(Data.FORMAT_UINT32, 0);
            if (mDuration < 0) {
                mDuration = 0;
            }

            mTripId = responseData.getIntValue(Data.FORMAT_UINT16, 4);
            if (mTripId < 0) {
                mTripId = 0;
            }

            // Streaming status available since firmware 6.1.8
            if (getMaxResponseDataSize() > 6 && responseData.size() > 6) {
                mStreaming = (responseData.getByte(6) > 0);
            }
        }

        return true;
    }

    @IntRange(from = 0)
    public int getTripId() {
        return mTripId;
    }

    @Nullable
    public CommandResponseCodeGetTrip getStatus() {
        return mStatus;
    }

    @NonNull
    public TripState getTripStatus() {
        if (mStatus == null) return TripState.STOPPED;

        switch (mStatus) {
            case TRIP_RUNNING:
                return TripState.RUNNING;
            case TRIP_STOPPED:
                return TripState.STOPPED;
            case TRIP_CALIBRATION:
                return TripState.STARTED_CALIBRATION;
            case TRIP_BSQ:
                return TripState.BSQ;
        }

        return TripState.STOPPED;
    }

    @IntRange(from = 0)
    public int getDuration() {
        return mDuration;
    }

    @IntRange(from = 0)
    public long getDurationMs() {
        return mDuration * 1000;
    }

    public boolean isStreaming() {
        return mStreaming;
    }
}
