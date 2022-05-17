package com.ellcie_healthy.ble_library.ble.profile.command.data.cmd;

import androidx.annotation.NonNull;

import no.nordicsemi.android.ble.data.Data;

abstract class CommandFallPreventionCommands extends CommandResponse {
    protected CommandResponseCodeDataPrevention mStatus;

    public enum FallPreventionCommandCode {
        GET_EVENT_INFOS(0x01),
        STOP_EVENT_STREAMING(0x50),
        START_EVENT_STREAMING(0x51),
        CONFIRM_EVENT_RECEIVED(0x60),
        RECEPTION_ERROR(0xFF);

        private final int code;

        FallPreventionCommandCode(int c) {
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

    public CommandFallPreventionCommands(final FallPreventionCommandCode command) {
        super(CommandCode.COMMAND_FALL_PREVENTION, command.getCode());
    }

    public CommandFallPreventionCommands(final FallPreventionCommandCode command, @NonNull byte[] data) {
        super(CommandCode.COMMAND_FALL_PREVENTION, concat(command.getCode(), data));
    }

    public CommandFallPreventionCommands(final FallPreventionCommandCode command, final int minResponseDataSize, final int maxResponseDataSize) {
        super(CommandCode.COMMAND_FALL_PREVENTION, command.getCode(), minResponseDataSize, maxResponseDataSize);
    }

    public CommandFallPreventionCommands(final FallPreventionCommandCode command, final int responseDataSize) {
        super(CommandCode.COMMAND_FALL_PREVENTION, command.getCode(), responseDataSize);
    }

    @Override
    protected boolean parseData(@NonNull final Data responseData) {
        mStatus = CommandResponseCodeDataPrevention.valueOf(getIntResponseCode());
        return mStatus != null;
    }

    public CommandResponseCodeDataPrevention getFallPreventionResponseStatus() {
        return mStatus;
    }
}
