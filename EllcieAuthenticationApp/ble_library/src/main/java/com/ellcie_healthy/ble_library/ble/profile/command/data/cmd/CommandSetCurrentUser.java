package com.ellcie_healthy.ble_library.ble.profile.command.data.cmd;

public class CommandSetCurrentUser extends CommandResponse {
    public CommandSetCurrentUser(final long user) {
        super(CommandCode.COMMAND_SET_CURRENT_USER,
                new byte[]{(byte) ((user >> 24) & 0xFF),
                        (byte) ((user >> 16) & 0xFF),
                        (byte) ((user >> 8) & 0xFF),
                        (byte) (user & 0xFF)});
    }
}
