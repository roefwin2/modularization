package com.ellcie_healthy.ble_library.ble.profile;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import androidx.annotation.NonNull;

import no.nordicsemi.android.ble.callback.profile.ProfileReadResponse;
import no.nordicsemi.android.ble.data.Data;

public class WriteResponseCallback extends ProfileReadResponse implements WriteStatusCallback {
    protected boolean mIsSent = true;

    @Override
    public void onDataSent(@NonNull BluetoothDevice device, @NonNull Data data) {
//        Log.d("WriteResponseCallback", "onDataSent: " + data);
        mIsSent = true;
    }

    @Override
    public void onRequestFailed(@NonNull BluetoothDevice device, int status) {
        Log.e("WriteResponseCallback", "onRequestFailed: " + status);
        mIsSent = false;
    }
}