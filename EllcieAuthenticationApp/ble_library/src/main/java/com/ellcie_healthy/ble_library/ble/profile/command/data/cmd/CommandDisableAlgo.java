package com.ellcie_healthy.ble_library.ble.profile.command.data.cmd;

public class CommandDisableAlgo extends CommandResponse {
    public CommandDisableAlgo(final boolean disable) {
        super(CommandCode.COMMAND_DEBUG_DISABLE_ALGO, (byte) (disable ? 0x0 : 0x1));
    }
}
