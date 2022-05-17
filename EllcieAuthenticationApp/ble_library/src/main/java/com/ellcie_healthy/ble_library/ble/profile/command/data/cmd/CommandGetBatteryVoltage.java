package com.ellcie_healthy.ble_library.ble.profile.command.data.cmd;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import no.nordicsemi.android.ble.data.Data;

public class CommandGetBatteryVoltage extends CommandResponse {
    private int mVoltage;

    public CommandGetBatteryVoltage() {
        super(CommandCode.COMMAND_GET_DEVICE_BATTERY_VOLTAGE, 4);
        mVoltage = 0;
    }

    @Override
    protected boolean parseData(@NonNull final Data responseData) {
        mVoltage = responseData.getIntValue(Data.FORMAT_UINT32, 0);

        return true;
    }

    @IntRange(from = 0)
    public int getVoltage() {
        return mVoltage;
    }
}
