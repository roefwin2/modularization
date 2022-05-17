package com.ellcie_healthy.ble_library.ble.profile.command.data.event;

import androidx.annotation.NonNull;

import com.ellcie_healthy.ble_library.ble.profile.command.data.FallState;

import java.security.InvalidParameterException;
import java.text.ParseException;
import java.util.Date;

import no.nordicsemi.android.ble.data.Data;

public class EventDataFall extends EventData {
    private FallState mState;
    private int mTimestamp;

    public EventDataFall(Data data) throws ParseException, InvalidParameterException {
        super(data, 5);

        mState = FallState.valueOf(data.getIntValue(Data.FORMAT_UINT8, EVENT_DATA_POS));
        if (mState == null) throw new ParseException("invalid state", EVENT_DATA_POS);

        mTimestamp = data.getIntValue(Data.FORMAT_UINT32, EVENT_DATA_POS + 1);
    }

    public FallState getState() {
        return mState;
    }

    public int getTimestamp() {
        return mTimestamp;
    }

    @NonNull
    @Override
    public String toString() {
        return "state: " + mState + " - ts: " + mTimestamp + " (" + new Date(mTimestamp * 1000) + ")";
    }
}
