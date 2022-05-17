package com.ellcie_healthy.ble_library.ble.profile.command.callback;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import androidx.annotation.NonNull;

import com.ellcie_healthy.ble_library.ble.profile.ReadStatusCallback;
import com.ellcie_healthy.ble_library.ble.profile.command.data.event.EventCode;
import com.ellcie_healthy.ble_library.ble.profile.command.data.event.EventData;
import com.ellcie_healthy.ble_library.ble.profile.command.data.event.EventDataFall;
import com.ellcie_healthy.ble_library.ble.profile.command.data.event.EventDataLocalize;
import com.ellcie_healthy.ble_library.ble.profile.command.data.event.EventDataShutdown;
import com.ellcie_healthy.ble_library.ble.profile.command.data.event.EventDataTap;
import com.ellcie_healthy.ble_library.ble.profile.command.data.event.EventDataTrip;
import com.ellcie_healthy.ble_library.ble.profile.command.data.event.EventDataWarning;

import java.security.InvalidParameterException;
import java.text.ParseException;

import no.nordicsemi.android.ble.callback.profile.ProfileDataCallback;
import no.nordicsemi.android.ble.data.Data;

public abstract class CommandEventDataCallback implements ProfileDataCallback, CommandEventCallback, ReadStatusCallback {
    private static final String TAG = "CmdEventDataCallback";

    @Override
    public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
        if (data.size() < EventData.EVENT_DATA_MIN_SIZE) {
            onInvalidDataReceived(device, data);
            return;
        }

        final EventCode eventCode = EventCode.valueOf(data.getIntValue(Data.FORMAT_UINT8, 0));
        if (eventCode == null) {
            onInvalidDataReceived(device, data);
            return;
        }

        try {
            switch (eventCode) {
                case LOCALIZE_MY_PHONE:
                    onEventLocalizeMyPhone(new EventDataLocalize(data));
                    break;
                case TRIP_STATE_CHANGE:
                    onEventTripStateChange(new EventDataTrip(data));
                    break;
                case WARNING:
                    onEventWarning(new EventDataWarning(data));
                    break;
                case TAPS_MODE:
                    onEventTapsMode(new EventDataTap(data));
                    break;
                case FALL_EVENT:
                    onEventFallStateChange(new EventDataFall(data));
                    break;
                case SHUTDOWN_INIIATED:
                    onEventShutdown(new EventDataShutdown(data));
                    break;
                case HARDWARE_FAULT:
                case SOFTWARE_FAULT:
                case REBOOT_INIIATED:
                default:
                    onUnimplementedEvent(eventCode, new EventData(data));
                    break;
            }
        } catch (ParseException | InvalidParameterException e) {
            Log.e(TAG, e.getMessage() + " - " + data);
            onInvalidDataReceived(device, data);
            return;
        }
    }
}
