package com.ellcie_healthy.ble_library.ble.profile.command.data.cmd;

public class CommandFallPreventionError extends CommandFallPreventionCommands {
    public CommandFallPreventionError(final int sequenceError) {
        super(FallPreventionCommandCode.RECEPTION_ERROR,
                new byte[]{(byte) (((sequenceError & 0xFF00) >> 8) & 0xFF), (byte) (sequenceError & 0xFF)});
    }
}
