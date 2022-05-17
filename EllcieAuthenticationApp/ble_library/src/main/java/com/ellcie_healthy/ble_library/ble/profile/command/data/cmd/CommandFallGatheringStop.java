package com.ellcie_healthy.ble_library.ble.profile.command.data.cmd;

public class CommandFallGatheringStop extends CommandFallGatheringCommands {
    public CommandFallGatheringStop() {
        super(FallGatheringCommandCode.STOP_EVENT_STREAMING);
    }
}
