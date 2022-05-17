package com.ellcie_healthy.ble_library.ble.profile.command.data.cmd;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import no.nordicsemi.android.ble.data.Data;

public class CommandGetBleAddress extends CommandResponse {
    private static final int BLE_ADDRESS_SIZE = 6;
    private byte[] mAddress = null;

    public CommandGetBleAddress() {
        super(CommandCode.COMMAND_GET_DEVICE_BT_ADDR, BLE_ADDRESS_SIZE);
    }

    @Override
    protected boolean parseData(@NonNull final Data responseData) {
        mAddress = responseData.getValue();

        return true;
    }

    @Nullable
    public byte[] getAddress() {
        return mAddress;
    }

    @Nullable
    public final String getStringAddress() {
        if (mAddress == null) return null;

        StringBuilder sb = new StringBuilder(mAddress.length * 2);
        for (int i = 0; i < mAddress.length; i++) {
            sb.append(String.format("%02X%s",
                    mAddress[i],
                    (i >= (mAddress.length - 1) ? "" : ":")));
        }
        return sb.toString();
    }
}
