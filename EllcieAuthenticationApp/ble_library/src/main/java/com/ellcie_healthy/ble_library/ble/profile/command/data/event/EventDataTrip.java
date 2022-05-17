package com.ellcie_healthy.ble_library.ble.profile.command.data.event;

import androidx.annotation.NonNull;

import com.ellcie_healthy.ble_library.ble.profile.command.data.TripState;
import com.ellcie_healthy.ble_library.ble.profile.command.data.TripStateInfo;
import com.ellcie_healthy.common.converters.Converters;

import java.security.InvalidParameterException;
import java.text.ParseException;

import no.nordicsemi.android.ble.data.Data;

public class EventDataTrip extends EventData {
    private TripState mState;
    private TripStateInfo mInfo;
    private int mTripId;
    private String mDriverTripId;

    public EventDataTrip(Data data) throws ParseException, InvalidParameterException {
        super(data, 4);

        mState = TripState.valueOf(data.getIntValue(Data.FORMAT_UINT8, EVENT_DATA_POS));
        if (mState == null) throw new ParseException("invalid state", EVENT_DATA_POS);

        mInfo = TripStateInfo.valueOf(data.getIntValue(Data.FORMAT_UINT8, EVENT_DATA_POS + 1));
        if (mInfo == null) throw new ParseException("invalid info", EVENT_DATA_POS + 1);

        mTripId = data.getIntValue(Data.FORMAT_UINT16, EVENT_DATA_POS + 2);

        mDriverTripId = Converters.getHexValue(data.getValue()[4]) + Converters.getHexValue(data.getValue()[3]);
    }

    public TripState getState() {
        return mState;
    }

    public TripStateInfo getStateInfo() {
        return mInfo;
    }

    public int getTripId() {
        return mTripId;
    }

    public String getDriverTripId() {
        return mDriverTripId;
    }

    @NonNull
    @Override
    public String toString() {
        return "state: " + mState + " - info: " + mInfo + " - trip: " + mTripId;
    }
}
