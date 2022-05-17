package com.ellcie_healthy.ble_library.ble.profile.command.data.cmd;

public class CommandFallPreventionStop extends CommandFallPreventionCommands {
    public CommandFallPreventionStop() {
        super(FallPreventionCommandCode.STOP_EVENT_STREAMING);
    }
}
