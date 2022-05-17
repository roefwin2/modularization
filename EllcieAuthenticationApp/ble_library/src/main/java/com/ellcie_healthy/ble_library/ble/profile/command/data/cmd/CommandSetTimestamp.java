package com.ellcie_healthy.ble_library.ble.profile.command.data.cmd;

import androidx.annotation.IntRange;

public class CommandSetTimestamp extends CommandResponse {
    public CommandSetTimestamp(@IntRange(from = 0) final long timestamp) {
        super(CommandCode.COMMAND_SET_TIMESTAMP,
                new byte[]{(byte) ((timestamp >> 24) & 0xFF),
                        (byte) ((timestamp >> 16) & 0xFF),
                        (byte) ((timestamp >> 8) & 0xFF),
                        (byte) (timestamp & 0xFF)});
    }
}
