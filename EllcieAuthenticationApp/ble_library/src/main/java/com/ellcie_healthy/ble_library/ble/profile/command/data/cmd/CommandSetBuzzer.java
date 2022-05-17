package com.ellcie_healthy.ble_library.ble.profile.command.data.cmd;

public class CommandSetBuzzer extends CommandResponse {
    public CommandSetBuzzer(final boolean enable) {
        super(CommandCode.COMMAND_SET_DEVICE_BUZZER, (byte) (enable ? 0x1 : 0x0));
    }
}
