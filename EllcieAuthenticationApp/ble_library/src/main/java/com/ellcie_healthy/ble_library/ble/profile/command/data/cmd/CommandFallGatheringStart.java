package com.ellcie_healthy.ble_library.ble.profile.command.data.cmd;

import androidx.annotation.IntRange;

public class CommandFallGatheringStart extends CommandFallGatheringCommands {

    public CommandFallGatheringStart(@IntRange(from = 0, to = 0xFF) final int sleepBetweenFrame,
                                     @IntRange(from = 0, to = 0xFF) final int sleepEveryNFrames,
                                     @IntRange(from = 0, to = 0xFF) final int sleepDurationBetweenFrame) {
        super(FallGatheringCommandCode.START_EVENT_STREAMING, new byte[]{
                (byte) (sleepBetweenFrame & 0xFF),
                (byte) (sleepEveryNFrames & 0xFF),
                (byte) (sleepDurationBetweenFrame & 0xFF),
        });
    }

    public CommandFallGatheringStart() {
        this(0, 0, 0);
    }
}
