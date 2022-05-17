package com.ellcie_healthy.ble_library.ble.profile.command.data.cmd;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import no.nordicsemi.android.ble.data.Data;

public class CommandFallPreventionGetEventInfo extends CommandFallPreventionCommands {
    private short dataSize;
    private short nbFrames;

    public CommandFallPreventionGetEventInfo() {
        super(FallPreventionCommandCode.GET_EVENT_INFOS, 0, 9);
    }

    @Override
    protected boolean parseData(@NonNull final Data responseData) {
        if (!super.parseData(responseData)) return false;

        dataSize = 0;
        nbFrames = 0;
        if (responseData.size() == 4) {
            final ByteBuffer bb = ByteBuffer.allocate(responseData.size()).
                    order(ByteOrder.BIG_ENDIAN)
                    .put(responseData.getValue());

            dataSize = bb.getShort(0);
            nbFrames = bb.getShort(2);
        } else if (responseData.size() != 0) {
            return false;
        }

        return true;
    }

    public short getDataSize() {
        return dataSize;
    }

    public short getNbFrames() {
        return nbFrames;
    }
}
