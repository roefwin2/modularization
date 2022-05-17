package com.ellcie_healthy.ble_library.ble.profile.command.data.cmd;

public class CommandDebugRisk extends CommandResponse {

    public CommandDebugRisk(byte riskLevel) {
        //super(CommandCode.COMMAND_STREET_LAB_GENERATE_ALARM);
        //super(CommandCode.COMMAND_DEBUG_GENERATE_TRIP_RISK, riskLevel);
        super(CommandCode.COMMAND_DEBUG_GENERATE_TRIP_RISK, riskLevel);
    }
}
