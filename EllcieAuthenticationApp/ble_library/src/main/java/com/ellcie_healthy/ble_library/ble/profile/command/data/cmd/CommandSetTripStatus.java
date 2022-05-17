package com.ellcie_healthy.ble_library.ble.profile.command.data.cmd;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ellcie_healthy.common.converters.Converters;

import no.nordicsemi.android.ble.data.Data;

public class CommandSetTripStatus extends CommandResponse {
    private static final int MAX_RESPONSE_DATA_SIZE = 2;

    private CommandResponseCodeSetTrip mStatus;
    private int tripId;
    private String driverTripId;

    public CommandSetTripStatus(final boolean start) {
        super(CommandCode.COMMAND_SET_TRIP_STATUS, (byte) (start ? 0x1 : 0x0), 0, MAX_RESPONSE_DATA_SIZE);
    }

    @Override
    protected boolean parseData(@NonNull Data responseData) {
        mStatus = CommandResponseCodeSetTrip.valueOf(getIntResponseCode());
        tripId = -1;
        if (responseData.size() == MAX_RESPONSE_DATA_SIZE) {
            tripId = responseData.getIntValue(Data.FORMAT_UINT16, 0);
        }
        if (responseData.getValue() != null && responseData.getValue().length > 0) {
            driverTripId = Converters.getHexValue(responseData.getValue()[1]) + Converters.getHexValue(responseData.getValue()[0]);
        }
        return (mStatus != null);
    }

    @Nullable
    public CommandResponseCodeSetTrip getStatus() {
        return mStatus;
    }

    public int getTripId() {
        return tripId;
    }

    public String getDriverTripId() {
        return driverTripId;
    }
}
