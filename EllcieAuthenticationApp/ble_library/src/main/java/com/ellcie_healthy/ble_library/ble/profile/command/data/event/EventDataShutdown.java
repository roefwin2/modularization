package com.ellcie_healthy.ble_library.ble.profile.command.data.event;

import androidx.annotation.NonNull;

import com.ellcie_healthy.ble_library.ble.profile.command.data.ShutdownReason;

import java.security.InvalidParameterException;
import java.text.ParseException;

import no.nordicsemi.android.ble.data.Data;

public class EventDataShutdown extends EventData {
    private ShutdownReason mReason;

    public EventDataShutdown(Data data) throws ParseException, InvalidParameterException {
        super(data, 1);

        mReason = ShutdownReason.valueOf(data.getIntValue(Data.FORMAT_UINT8, EVENT_DATA_POS));
        if (mReason == null) throw new ParseException("invalid shutdown reason", EVENT_DATA_POS);
    }

    public ShutdownReason getCode() {
        return mReason;
    }

    @NonNull
    @Override
    public String toString() {
        return "reason: " + mReason;
    }
}
