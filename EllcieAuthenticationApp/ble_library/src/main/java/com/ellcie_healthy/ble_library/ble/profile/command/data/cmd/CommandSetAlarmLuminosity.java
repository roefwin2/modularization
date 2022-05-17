package com.ellcie_healthy.ble_library.ble.profile.command.data.cmd;

import androidx.annotation.IntRange;

public class CommandSetAlarmLuminosity extends CommandResponse {

    public CommandSetAlarmLuminosity(@IntRange(from = 0, to = 100) final int luminosity) {
        super(CommandCode.COMMAND_SET_ALARM_LUMINOSITY, (byte) luminosity);
    }
}
