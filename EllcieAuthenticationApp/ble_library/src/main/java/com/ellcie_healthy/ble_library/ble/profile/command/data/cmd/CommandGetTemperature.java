package com.ellcie_healthy.ble_library.ble.profile.command.data.cmd;

import androidx.annotation.NonNull;

import no.nordicsemi.android.ble.data.Data;

public class CommandGetTemperature extends CommandResponse {
    private float mTempHts221 = 0; // in °C
    private float mTempLps22hb = 0; // in °C

    public CommandGetTemperature() {
        super(CommandCode.COMMAND_GET_DEVICE_TEMPERATURE, 8);
    }

    @Override
    protected boolean parseData(@NonNull final Data responseData) {
        mTempHts221 = responseData.getIntValue(Data.FORMAT_SINT32, 0) / 10000;
        mTempLps22hb = responseData.getIntValue(Data.FORMAT_SINT32, 4) / 10000;

        return true;
    }

    public float getTempHts221() {
        return mTempHts221;
    }

    public float getTempLps22hb() {
        return mTempLps22hb;
    }
}
