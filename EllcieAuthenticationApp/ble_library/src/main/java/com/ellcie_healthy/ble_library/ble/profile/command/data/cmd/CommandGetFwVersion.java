package com.ellcie_healthy.ble_library.ble.profile.command.data.cmd;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import no.nordicsemi.android.ble.data.Data;

public class CommandGetFwVersion extends CommandResponse {
    private static final int MIN_VERSION_SIZE = 5;
    private static final int MAX_VERSION_SIZE = 16;
    private String mVersion = null;

    public CommandGetFwVersion() {
        super(CommandCode.COMMAND_GET_DEVICE_FIRWMARE_VERSION, MIN_VERSION_SIZE, MAX_VERSION_SIZE);
    }

    @Override
    protected boolean parseData(@NonNull final Data responseData) {
        mVersion = responseData.getStringValue(0);

        return true;
    }

    @Nullable
    public String getVersion() {
        return mVersion;
    }
}
