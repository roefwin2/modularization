package com.ellcie_healthy.ble_library.ble.profile.command.data.cmd;

import androidx.annotation.NonNull;

import com.ellcie_healthy.ble_library.ble.profile.command.data.SensorType;

import java.security.InvalidParameterException;
import java.util.ArrayList;

public class CommandSetStreaming extends CommandResponse {
    public CommandSetStreaming(@NonNull ArrayList<SensorType> sensors) {
        super(CommandCode.COMMAND_DATA_STREAMING_CONTROL);

        byte[] bSensors = new byte[sensors.size()];
        for (int i = 0; i < sensors.size(); i++) {
            SensorType sensor = sensors.get(i);
            if (sensor == null || sensor == SensorType.DISABLE)
                throw new InvalidParameterException("Sensor " + sensor + " is invalid");
            bSensors[i] = (byte) sensor.getCode();
        }

        this.addQueryData(bSensors);
    }

    public CommandSetStreaming(@NonNull SensorType sensor) {
        super(CommandCode.COMMAND_DATA_STREAMING_CONTROL);

        if (sensor == SensorType.DISABLE) {
            // nothing to do
            return;
        }

        this.addQueryData((byte) sensor.getCode());
    }
}
