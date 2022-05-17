package com.ellcie_healthy.ble_library.ble.profile.fota.callback;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

import com.ellcie_healthy.ble_library.ble.profile.ReadStatusCallback;
import com.ellcie_healthy.ble_library.ble.profile.fota.data.FotaEvent;

import no.nordicsemi.android.ble.callback.profile.ProfileDataCallback;
import no.nordicsemi.android.ble.data.Data;

public abstract class FotaEventDataCallback implements ProfileDataCallback, FotaEventCallback, ReadStatusCallback {
    @Override
    public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
        if (data.size() != 4) {
            onInvalidDataReceived(device, data);
            return;
        }


        final FotaEvent event = new FotaEvent(FotaEvent.FotaEventType.valueOf(data.getIntValue(Data.FORMAT_UINT16, 0)), data.getIntValue(Data.FORMAT_UINT16, 2));
        onFotaEvent(device, event);
    }
}
