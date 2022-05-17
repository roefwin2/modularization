package com.ellcie_healthy.ble_library.ble.profile.command.data.cmd;

public class CommandFallGatheringError extends CommandFallGatheringCommands {
    public CommandFallGatheringError(final int sequenceError) {
        super(FallGatheringCommandCode.RECEPTION_ERROR,
                new byte[]{(byte) (((sequenceError & 0xFF00) >> 8) & 0xFF), (byte) (sequenceError & 0xFF)});
    }
}
