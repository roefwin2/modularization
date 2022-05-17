package com.ellcie_healthy.ble_library.ble.profile.command.data.cmd;

import androidx.annotation.NonNull;

import no.nordicsemi.android.ble.data.Data;

public class CommandFallGatheringGetNbEvents extends CommandFallGatheringCommands {
    private int nbEvents = 0;

    public CommandFallGatheringGetNbEvents() {
        super(FallGatheringCommandCode.GET_NB_EVENTS, 1);
    }

    @Override
    protected boolean parseData(@NonNull final Data responseData) {
        if (!super.parseData(responseData)) return false;

        nbEvents = responseData.getIntValue(Data.FORMAT_UINT8, 0);

        return true;
    }

    public int getNbEvents() {
        return nbEvents;
    }
}
