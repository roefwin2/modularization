package com.ellcie_healthy.ble_library.ble.profile.command.data.event;

import androidx.annotation.NonNull;

import com.ellcie_healthy.ble_library.ble.profile.command.data.SilentModeReason;

import java.security.InvalidParameterException;
import java.text.ParseException;

import no.nordicsemi.android.ble.data.Data;

public class EventDataSilentMode extends EventData {
    private SilentModeReason mReason;

    public EventDataSilentMode(Data data) throws ParseException, InvalidParameterException {
        super(data, 1);

        mReason = SilentModeReason.valueOf(data.getIntValue(Data.FORMAT_UINT8, EVENT_DATA_POS));
        if (mReason == null) throw new ParseException("invalid shutdown reason", EVENT_DATA_POS);
    }

    public SilentModeReason getCode() {
        return mReason;
    }

    @NonNull
    @Override
    public String toString() {
        return "reason: " + mReason;
    }
}
