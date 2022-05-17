package com.ellcie_healthy.ble_library.ble.profile.command.data.cmd;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import no.nordicsemi.android.ble.data.Data;

public class CommandGetDeviceName extends CommandResponse {
    private static final int MIN_DEVICE_NAME_SIZE = 0;
    private static final int MAX_DEVICE_NAME_SIZE = 16;
    private String mName = null;

    public CommandGetDeviceName() {
        super(CommandCode.COMMAND_GET_DEVICE_NAME, MIN_DEVICE_NAME_SIZE, MAX_DEVICE_NAME_SIZE);
    }

    @Override
    protected boolean parseData(@NonNull final Data responseData) {
        mName = responseData.getStringValue(0);

        return true;
    }

    @Nullable
    public String getName() {
        return mName;
    }
}
