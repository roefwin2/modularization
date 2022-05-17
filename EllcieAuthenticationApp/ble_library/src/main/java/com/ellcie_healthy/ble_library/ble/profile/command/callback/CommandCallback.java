package com.ellcie_healthy.ble_library.ble.profile.command.callback;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

import no.nordicsemi.android.ble.data.Data;

public interface CommandCallback {
    void onCommandResponse(@NonNull final BluetoothDevice device, @NonNull final Data data);

    void onCommandTimeout();

    void onCommandError(@NonNull final BluetoothDevice device, final Data data);
}
