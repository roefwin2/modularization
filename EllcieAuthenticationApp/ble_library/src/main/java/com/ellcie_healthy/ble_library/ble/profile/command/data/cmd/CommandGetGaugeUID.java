package com.ellcie_healthy.ble_library.ble.profile.command.data.cmd;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import no.nordicsemi.android.ble.data.Data;

public class CommandGetGaugeUID extends CommandResponse {
    private static final int GAUGE_UID_SIZE = 16;

    private byte[] mUid = null;

    public CommandGetGaugeUID() {
        super(CommandCode.COMMAND_GET_DEVICE_GAUGE_UID, GAUGE_UID_SIZE);
    }

    @Override
    protected boolean parseData(@NonNull Data responseData) {
        mUid = responseData.getValue();
        return true;
    }

    @Nullable
    public final byte[] getUid() {
        return mUid;
    }

    @Nullable
    public final String getStringUid() {
        if (mUid == null) return null;

        StringBuilder sb = new StringBuilder(mUid.length * 2);
        for (byte b : mUid)
            sb.append(String.format("%02X", b));
        return sb.toString();
    }
}
