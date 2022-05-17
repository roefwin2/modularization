package com.ellcie_healthy.ble_library.ble.profile.command.data.cmd;

import androidx.annotation.IntRange;

public class CommandSetNotifyLuminosity extends CommandResponse {

    public CommandSetNotifyLuminosity(@IntRange(from = 0, to = 100) final int luminosity) {
        super(CommandCode.COMMAND_SET_NOTIF_LUMINOSITY, (byte) luminosity);
    }
}
