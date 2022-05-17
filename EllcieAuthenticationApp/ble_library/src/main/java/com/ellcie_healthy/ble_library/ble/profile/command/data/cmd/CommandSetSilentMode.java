package com.ellcie_healthy.ble_library.ble.profile.command.data.cmd;

public class CommandSetSilentMode extends CommandResponse {
    public CommandSetSilentMode(final boolean enable) {
        super(CommandCode.COMMAND_GET_SET_SILENT_MODE, (byte) (enable ? 0x1 : 0x0));
    }
}
