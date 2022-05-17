package com.ellcie_healthy.ble_library.ble.profile.command.data.cmd;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import no.nordicsemi.android.ble.data.Data;

public class CommandGetBatterySoc extends CommandResponse {
    private int mBattery;

    public CommandGetBatterySoc() {
        super(CommandCode.COMMAND_GET_BATTERY_LEVEL, 1);
        mBattery = 0;
    }

    @Override
    protected boolean parseData(@NonNull final Data responseData) {
        mBattery = responseData.getIntValue(Data.FORMAT_UINT8, 0);
        if (mBattery > 100) mBattery = 100;
        if (mBattery < 0) mBattery = 0;

        return true;
    }

    @IntRange(from = 0, to = 100)
    public int getBattery() {
        return mBattery;
    }
}
