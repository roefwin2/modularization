package com.ellcie_healthy.ble_library.ble.profile.command.data.cmd;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import no.nordicsemi.android.ble.data.Data;

public class CommandGetSerial extends CommandResponse {
    private static final int SERIAL_SIZE = 16;
    private String mSerial = null;

    public CommandGetSerial() {
        super(CommandCode.COMMAND_GET_DEVICE_SERIAL_NUMBER, SERIAL_SIZE);
    }

    @Override
    protected boolean parseData(@NonNull final Data responseData) {
        mSerial = responseData.getStringValue(0);

        return true;
    }

    @Nullable
    public String getSerial() {
        return mSerial;
    }
}
