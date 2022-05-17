package com.ellcie_healthy.ble_library.ble.profile.command.data.cmd;

import androidx.annotation.NonNull;

import no.nordicsemi.android.ble.data.Data;

public class CommandGetLuminosity extends CommandResponse {
    private int mLuminosity = 0;

    public CommandGetLuminosity() {
        super(CommandCode.COMMAND_GET_DEVICE_LUMINOSITY, 4);
    }

    @Override
    protected boolean parseData(@NonNull final Data responseData) {
        mLuminosity = responseData.getIntValue(Data.FORMAT_SINT32, 0);

        return true;
    }

    public int getLuminosity() {
        return mLuminosity;
    }
}
