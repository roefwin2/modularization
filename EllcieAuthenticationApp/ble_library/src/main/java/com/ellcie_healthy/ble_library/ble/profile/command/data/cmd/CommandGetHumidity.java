package com.ellcie_healthy.ble_library.ble.profile.command.data.cmd;

import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;

import no.nordicsemi.android.ble.data.Data;

public class CommandGetHumidity extends CommandResponse {
    private float mHumidity = 0; // in %

    public CommandGetHumidity() {
        super(CommandCode.COMMAND_GET_DEVICE_HUMIDITY, 4);
    }

    @Override
    protected boolean parseData(@NonNull final Data responseData) {
        mHumidity = responseData.getIntValue(Data.FORMAT_SINT32, 0) / 10000;
        if (mHumidity > 100) mHumidity = 100;
        if (mHumidity < 0) mHumidity = 0;

        return true;
    }

    @FloatRange(from = 0, to = 100)
    public float getHumidity() {
        return mHumidity;
    }
}
