package com.ellcie_healthy.ble_library.ble.profile.command.data.cmd;

import androidx.annotation.NonNull;

import no.nordicsemi.android.ble.data.Data;

abstract class CommandFallGatheringCommands extends CommandResponse {
    protected CommandResponseCodeDataGathering mStatus;

    public enum FallGatheringCommandCode {
        GET_NB_EVENTS(0x00),
        GET_EVENT_INFOS(0x01),
        STOP_EVENT_STREAMING(0x50),
        START_EVENT_STREAMING(0x51),
        CONFIRM_EVENT_RECEIVED(0x60),
        RECEPTION_ERROR(0xFF);

        private final int code;

        FallGatheringCommandCode(int c) {
            code = c;
        }

        public byte getCode() {
            return (byte) code;
        }
    }

    private static byte[] concat(byte code, byte[] data) {

        byte[] fullData = new byte[data.length + 1];
        System.arraycopy(data, 0, fullData, 1, data.length);
        fullData[0] = code;

        return fullData;
    }

    public CommandFallGatheringCommands(final FallGatheringCommandCode command) {
        super(CommandCode.COMMAND_FALL_DATA_GATHERING, command.getCode());
    }

    public CommandFallGatheringCommands(final FallGatheringCommandCode command, @NonNull byte[] data) {
        super(CommandCode.COMMAND_FALL_DATA_GATHERING, concat(command.getCode(), data));
    }

    public CommandFallGatheringCommands(final FallGatheringCommandCode command, final int minResponseDataSize, final int maxResponseDataSize) {
        super(CommandCode.COMMAND_FALL_DATA_GATHERING, command.getCode(), minResponseDataSize, maxResponseDataSize);
    }

    public CommandFallGatheringCommands(final FallGatheringCommandCode command, final int responseDataSize) {
        super(CommandCode.COMMAND_FALL_DATA_GATHERING, command.getCode(), responseDataSize);
    }

    @Override
    protected boolean parseData(@NonNull final Data responseData) {
        mStatus = CommandResponseCodeDataGathering.valueOf(getIntResponseCode());

        if (mStatus == null) {
            return false;
        }

        return true;
    }

    public CommandResponseCodeDataGathering getFallGatheringResponseStatus() {
        return mStatus;
    }
}
