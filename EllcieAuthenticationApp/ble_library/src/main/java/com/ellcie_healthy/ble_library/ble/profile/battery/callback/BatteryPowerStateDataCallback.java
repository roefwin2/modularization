package com.ellcie_healthy.ble_library.ble.profile.battery.callback;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

import com.ellcie_healthy.ble_library.ble.profile.ReadStatusCallback;

import no.nordicsemi.android.ble.callback.profile.ProfileDataCallback;
import no.nordicsemi.android.ble.data.Data;

public abstract class BatteryPowerStateDataCallback implements ProfileDataCallback, BatteryPowerStateCallback, ReadStatusCallback {
    //    private static final int BATTERY_POWER_STATE_UNKNOWN = 0;
//    private static final int BATTERY_POWER_STATE_NOT_SUPPORTED = 1;
    private static final int BATTERY_POWER_STATE_FALSE = 2;
    private static final int BATTERY_POWER_STATE_TRUE = 3;

    private static final int BATTERY_PARAM_MASK = 0x3;
    private static final int POWER_PRESENCE_SHIFT = 0;
    private static final int BATTERY_DISCHARGING_SHIFT = 2;
    private static final int BATTERY_CHARGING_SHIFT = 4;

    @Override
    public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
        if (data.size() != 1) {
            onInvalidDataReceived(device, data);
            return;
        }

        final byte state = data.getByte(0);
        final int presence = (state >> POWER_PRESENCE_SHIFT) & BATTERY_PARAM_MASK;
        final int charging = (state >> BATTERY_CHARGING_SHIFT) & BATTERY_PARAM_MASK;
        final int discharging = (state >> BATTERY_DISCHARGING_SHIFT) & BATTERY_PARAM_MASK;

        Boolean isPlugged = null;
        Boolean isCharging = null;

//        Log.d("toto", data.toString() + " - " + String.format("%8s", Integer.toBinaryString(state & 0xFF)).replace(' ', '0'));
//        Log.d("toto", "charging: " + charging + " - discharging: " + discharging);

        if (presence == BATTERY_POWER_STATE_FALSE) {
            isPlugged = new Boolean(false);
        } else if (presence == BATTERY_POWER_STATE_TRUE) {
            isPlugged = new Boolean(true);
        } else {
            onInvalidDataReceived(device, data);
            return;
        }

        if ((charging == BATTERY_POWER_STATE_TRUE) && (discharging == BATTERY_POWER_STATE_FALSE)) {
            isCharging = new Boolean(true);
        } else if ((charging == BATTERY_POWER_STATE_FALSE) && (discharging == BATTERY_POWER_STATE_TRUE)) {
            isCharging = new Boolean(false);
        } else {
            onInvalidDataReceived(device, data);
            return;
        }

        onBatteryPowerState(device, isPlugged, isCharging);
    }
}
