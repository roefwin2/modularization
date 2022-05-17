package com.ellcie_healthy.ble_library.ble.profile.command.data.event;

import androidx.annotation.NonNull;

import com.ellcie_healthy.ble_library.ble.profile.command.data.WarningCode;

import java.security.InvalidParameterException;
import java.text.ParseException;

import no.nordicsemi.android.ble.data.Data;

public class EventDataWarning extends EventData {
    private WarningCode mCode;

    public EventDataWarning(Data data) throws ParseException, InvalidParameterException {
        super(data, 1);

        mCode = WarningCode.valueOf(data.getIntValue(Data.FORMAT_UINT8, EVENT_DATA_POS));
        if (mCode == null) throw new ParseException("invalid warning code", EVENT_DATA_POS);
    }

    public WarningCode getCode() {
        return mCode;
    }

    @NonNull
    @Override
    public String toString() {
        return "code: " + mCode;
    }
}
