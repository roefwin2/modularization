package com.ellcie_healthy.ble_library.ble.profile.command.data.cmd;

import androidx.annotation.NonNull;

import no.nordicsemi.android.ble.data.Data;

public class CommandGetFallStatus extends CommandResponse {
    private long timestamp = 0;
    private CommandResponseCodeGetFallStatus mStatus = CommandResponseCodeGetFallStatus.NO_EVENT;

    public CommandGetFallStatus() {
        super(CommandCode.COMMAND_GET_FALL_STATUS, 4);
    }

    @Override
    protected boolean parseData(@NonNull final Data responseData) {
        mStatus = CommandResponseCodeGetFallStatus.valueOf(getIntResponseCode());

        if (mStatus == null) {
            return false;
        }

        timestamp = responseData.getIntValue(Data.FORMAT_UINT32, 0) * 1000; // convert it to ms

        return true;
    }

    public CommandResponseCodeGetFallStatus getFallStatus() {
        return mStatus;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
