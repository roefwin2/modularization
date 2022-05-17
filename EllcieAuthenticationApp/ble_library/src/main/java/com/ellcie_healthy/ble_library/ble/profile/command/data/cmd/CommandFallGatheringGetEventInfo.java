package com.ellcie_healthy.ble_library.ble.profile.command.data.cmd;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import no.nordicsemi.android.ble.data.Data;

public class CommandFallGatheringGetEventInfo extends CommandFallGatheringCommands {
    private short dataSize;
    private short nbFrames;
    private int id;
    private int type;

    public CommandFallGatheringGetEventInfo() {
        super(FallGatheringCommandCode.GET_EVENT_INFOS, 0, 9);
    }

    @Override
    protected boolean parseData(@NonNull final Data responseData) {
        if (!super.parseData(responseData)) return false;

        dataSize = 0;
        nbFrames = 0;
        if (responseData.size() == 9) {
            final ByteBuffer bb = ByteBuffer.allocate(responseData.size()).
                    order(ByteOrder.BIG_ENDIAN)
                    .put(responseData.getValue());

            dataSize = bb.getShort(0);
            nbFrames = bb.getShort(2);
            id = bb.getInt(4);
            type = bb.get(8);
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

    public int getId() {
        return id;
    }

    public int getType() {
        return type;
    }
}
