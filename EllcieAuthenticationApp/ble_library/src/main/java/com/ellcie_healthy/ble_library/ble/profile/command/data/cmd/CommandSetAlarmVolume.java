package com.ellcie_healthy.ble_library.ble.profile.command.data.cmd;

import androidx.annotation.IntRange;

public class CommandSetAlarmVolume extends CommandResponse {

    public CommandSetAlarmVolume(@IntRange(from = 0, to = 10) final int volume) {
        super(CommandCode.COMMAND_SET_ALARM_VOLUME, (byte) volume);
    }
}
