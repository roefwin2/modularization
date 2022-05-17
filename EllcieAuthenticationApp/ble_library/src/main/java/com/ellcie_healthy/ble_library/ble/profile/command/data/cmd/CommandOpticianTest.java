package com.ellcie_healthy.ble_library.ble.profile.command.data.cmd;

import androidx.annotation.NonNull;

import no.nordicsemi.android.ble.data.Data;

public class CommandOpticianTest extends CommandResponse {

    private int mNbBlink;

    public CommandOpticianTest(int i) {
        super(new byte[]{CommandCode.COMMAND_OPTICIAN_TEST.getCode(), (byte) i}, 0, 1);
    }

    @Override
    protected boolean parseData(@NonNull Data responseData) {
        if (!super.parseData(responseData)) return false;
        if(responseData.getIntValue(Data.FORMAT_UINT8, 0) != null) {//Same command in start and stop, only the stop has this information
            mNbBlink = responseData.getValue()[0];
        }
        return true;
    }


    public int getNbBlink(){
        return  mNbBlink;
    }
}
