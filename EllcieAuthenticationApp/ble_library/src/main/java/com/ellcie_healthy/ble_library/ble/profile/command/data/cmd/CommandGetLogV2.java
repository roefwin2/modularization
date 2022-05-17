package com.ellcie_healthy.ble_library.ble.profile.command.data.cmd;

import androidx.annotation.NonNull;

import com.ellcie_healthy.ble_library.ble.profile.command.data.LogV2;

import java.util.ArrayList;
import java.util.Arrays;

import no.nordicsemi.android.ble.data.Data;

public class CommandGetLogV2 extends CommandResponse {
    private static final int MIN_LOGS_SIZE = 0;
    private static final int MAX_LOGS_SIZE = 16;
    private CommandGetLogStatus mStatus = CommandGetLogStatus.LOG_STATUS_NO_LOG;
    private ArrayList<LogV2> mLogs = null;

    public CommandGetLogV2() {
        super(CommandCode.COMMAND_GET_LOG, MIN_LOGS_SIZE, MAX_LOGS_SIZE);

        mLogs = new ArrayList<>();
    }

    @Override
    protected boolean parseData(@NonNull final Data responseData) {
        int code = getIntResponseCode();
        mStatus = (code >= 0 && code < CommandGetLogStatus.values().length) ? CommandGetLogStatus.values()[code] : null;
        if (mStatus == null) {
            return false;
        }

        byte[] data = responseData.getValue();
        mLogs.clear();

        switch (mStatus) {
            case LOG_STATUS_OK:
            case LOG_STATUS_TWO_LAST:
                mLogs.add(new LogV2(Arrays.copyOfRange(data, 0, LogV2.LOG_V2_DATA_SIZE)));
                mLogs.add(new LogV2(Arrays.copyOfRange(data, LogV2.LOG_V2_DATA_SIZE, MAX_LOGS_SIZE)));
                break;
            case LOG_STATUS_LAST:
                mLogs.add(new LogV2(Arrays.copyOfRange(data, 0, LogV2.LOG_V2_DATA_SIZE)));
                break;
            case LOG_STATUS_NO_LOG:
                break;
        }

        return true;
    }

    public CommandGetLogStatus getStatus() {
        return mStatus;
    }

    @NonNull
    public ArrayList<LogV2> getLogs() {
        return mLogs;
    }
}
