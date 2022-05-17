package com.ellcie_healthy.ble_library.ble.profile.command.data.cmd;

import androidx.annotation.NonNull;

import no.nordicsemi.android.ble.data.Data;

public class CommandGetPressure extends CommandResponse {
    private float mPressure = 0; // in hPa

    public CommandGetPressure() {
        super(CommandCode.COMMAND_GET_DEVICE_PRESSURE, 4);
    }

    @Override
    protected boolean parseData(@NonNull final Data responseData) {
        mPressure = responseData.getIntValue(Data.FORMAT_SINT32, 0) / 10000;

        return true;
    }

    public float getPressure() {
        return mPressure;
    }
}
