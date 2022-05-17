package com.ellcie_healthy.ble_library.ble.profile.command.data.cmd;

public class CommandFallGatheringConfirm extends CommandFallGatheringCommands {

    public CommandFallGatheringConfirm() {
        super(FallGatheringCommandCode.CONFIRM_EVENT_RECEIVED);
    }
}
