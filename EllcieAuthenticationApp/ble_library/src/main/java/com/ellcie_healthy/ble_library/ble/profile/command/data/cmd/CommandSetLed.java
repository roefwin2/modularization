package com.ellcie_healthy.ble_library.ble.profile.command.data.cmd;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

public class CommandSetLed extends CommandResponse {
    public CommandSetLed(@IntRange(from = 0, to = 0xFF) final int red,
                         @IntRange(from = 0, to = 0xFF) final int green,
                         @IntRange(from = 0, to = 0xFF) final int blue,
                         @NonNull final LedPosition position) {
        super(CommandCode.COMMAND_SET_DEVICE_LED,
                new byte[]{(byte) red, (byte) green, (byte) blue, (byte) position.code});
    }

    public enum LedPosition {
        LEFT(0x00),
        RIGHT(0x01),
        BOTH(0x02);

        private final int code;

        LedPosition(int c) {
            code = c;
        }
    }
}
