package com.ellcie_healthy.ble_library.ble.profile.command.data.event;

import androidx.annotation.NonNull;

import java.security.InvalidParameterException;
import java.text.ParseException;

import no.nordicsemi.android.ble.data.Data;

public class EventDataTap extends EventData {
    private boolean mEnable;

    public EventDataTap(Data data) throws ParseException, InvalidParameterException {
        super(data, 1);

        int val = data.getIntValue(Data.FORMAT_UINT8, EVENT_DATA_POS);
        if (val != 0 && val != 1) throw new ParseException("invalid tap value", EVENT_DATA_POS);

        mEnable = val == 1 ? false : true;
    }

    public boolean areTapEnable() {
        return mEnable;
    }

    @NonNull
    @Override
    public String toString() {
        return mEnable ? "enable" : "disable";
    }
}
