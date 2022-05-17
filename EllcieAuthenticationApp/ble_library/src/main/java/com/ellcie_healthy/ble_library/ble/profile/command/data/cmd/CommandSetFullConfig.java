package com.ellcie_healthy.ble_library.ble.profile.command.data.cmd;

import androidx.annotation.IntRange;

public class CommandSetFullConfig extends CommandResponse {
    public CommandSetFullConfig(@IntRange(from = 0, to = 10) final int alarmVolume,
                                @IntRange(from = 0, to = 10) final int notifVolume,
                                @IntRange(from = 0, to = 100) final int alarmLuminosity,
                                @IntRange(from = 0, to = 100) final int notifLuminosity,
                                final boolean silentMode,
                                @IntRange(from = 0) final long timestamp,
                                final boolean enableSos,
                                final boolean enableFaiting) {
        super(CommandCode.COMMAND_SET_FULL_CONFIG,
                new byte[]{
                        (byte) alarmVolume,
                        (byte) notifVolume,
                        (byte) alarmLuminosity,
                        (byte) notifLuminosity,
                        (byte) (silentMode ? 0x1 : 0x0),
                        (byte) ((timestamp >> 24) & 0xFF),
                        (byte) ((timestamp >> 16) & 0xFF),
                        (byte) ((timestamp >> 8) & 0xFF),
                        (byte) (timestamp & 0xFF),
                        (byte) ((enableSos ? 0x1 : 0x0) | (enableFaiting ? 0x1 : 0x0) << 1)
                });
    }

    public CommandSetFullConfig(@IntRange(from = 0, to = 10) final int alarmVolume,
                                @IntRange(from = 0, to = 10) final int notifVolume,
                                @IntRange(from = 0, to = 100) final int alarmLuminosity,
                                @IntRange(from = 0, to = 100) final int notifLuminosity,
                                final boolean silentMode,
                                @IntRange(from = 0) final long timestamp,
                                final boolean enableSos,
                                final boolean enableFaiting,
                                @IntRange(from = 1, to = 5) final int sensitivityLevel
                                ) {
        super(CommandCode.COMMAND_SET_FULL_CONFIG,
                new byte[]{
                        (byte) alarmVolume,
                        (byte) notifVolume,
                        (byte) alarmLuminosity,
                        (byte) notifLuminosity,
                        (byte) (silentMode ? 0x1 : 0x0),
                        (byte) ((timestamp >> 24) & 0xFF),
                        (byte) ((timestamp >> 16) & 0xFF),
                        (byte) ((timestamp >> 8) & 0xFF),
                        (byte) (timestamp & 0xFF),
                        (byte) ((enableSos ? 0x1 : 0x0) | (enableFaiting ? 0x1 : 0x0) << 1),
                        (byte) (0x0),
                        (byte) sensitivityLevel
                });
    }
}
