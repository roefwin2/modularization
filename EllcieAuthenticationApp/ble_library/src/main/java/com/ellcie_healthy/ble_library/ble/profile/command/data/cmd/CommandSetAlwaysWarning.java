package com.ellcie_healthy.ble_library.ble.profile.command.data.cmd;

public class CommandSetAlwaysWarning  extends CommandResponse {

    public CommandSetAlwaysWarning(final boolean enable) {
        super(CommandCode.COMMAND_SET_ALWAYS_WARNING, (byte) (enable ? 0x1 : 0x0));
    }
}