package com.ellcie_healthy.ble_library.ble.profile.command.data.cmd;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import no.nordicsemi.android.ble.data.Data;

public class CommandGetModel extends CommandResponse {
    private static final int MIN_MODEL_SIZE = 8;
    private static final int MAX_MODEL_SIZE = 14;
    private String mModel = null;

    public CommandGetModel() {
        super(CommandCode.COMMAND_GET_DEVICE_MODEL, MIN_MODEL_SIZE, MAX_MODEL_SIZE);
    }

    @Override
    protected boolean parseData(@NonNull final Data responseData) {
        mModel = responseData.getStringValue(0);

        return true;
    }

    @Nullable
    public String getModel() {
        return mModel;
    }
}
