package com.ellcie_healthy.ble_library.ble.profile.command.data.cmd;

public class CommandFallConfig extends CommandResponse {
    public CommandFallConfig(final boolean sos, final boolean fainting) {
        super(CommandCode.COMMAND_FALL_CONFIG, (byte) ((byte) (sos ? 0x01 : 0x0) | (byte) (fainting ? 0x02 : 0x0)));
    }
}
