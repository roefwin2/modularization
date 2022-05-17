package com.ellcie_healthy.ble_library.ble.profile.command.data.cmd;

import androidx.annotation.NonNull;

import no.nordicsemi.android.ble.data.Data;

public class CommandFallCommands extends CommandResponse {
    private long timestamp = 0;

    public enum FallCommandCode {
        ENABLE_FALL(0xEF),
        DISABLE_FALL(0xDF),
        CONFIRM_FALL(0xCF),
        CANCEL_FALL(0x0F),
        ENGAGE_SOS(0xE5),
        CONFIRM_SOS(0xC5),
        CANCEL_SOS(0x05),
        ACK(0xAC);

        private final int code;

        FallCommandCode(int c) {
            code = c;
        }

        public byte getCode() {
            return (byte) code;
        }
    }

    public CommandFallCommands(final FallCommandCode command) {
        super(CommandCode.COMMAND_FALL_COMMANDS, command.getCode(), 4, 4);
    }

    @Override
    protected boolean parseData(@NonNull final Data responseData) {
        timestamp = responseData.getIntValue(Data.FORMAT_UINT32, 0) * 1000; // convert it to ms
        return true;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
