package com.ellcie_healthy.ble_library.ble.profile.command;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import androidx.annotation.NonNull;

import com.ellcie_healthy.ble_library.ble.profile.BleCharacteristic;
import com.ellcie_healthy.ble_library.ble.profile.BleService;
import com.ellcie_healthy.ble_library.ble.profile.BleWriteCharacteristic;
import com.ellcie_healthy.ble_library.ble.profile.EHBleManager;
import com.ellcie_healthy.ble_library.ble.profile.command.callback.CommandEventCallback;
import com.ellcie_healthy.ble_library.ble.profile.command.callback.CommandEventDataCallback;
import com.ellcie_healthy.ble_library.ble.profile.command.data.event.EventCode;
import com.ellcie_healthy.ble_library.ble.profile.command.data.event.EventData;
import com.ellcie_healthy.ble_library.ble.profile.command.data.event.EventDataFall;
import com.ellcie_healthy.ble_library.ble.profile.command.data.event.EventDataLocalize;
import com.ellcie_healthy.ble_library.ble.profile.command.data.event.EventDataShutdown;
import com.ellcie_healthy.ble_library.ble.profile.command.data.event.EventDataSilentMode;
import com.ellcie_healthy.ble_library.ble.profile.command.data.event.EventDataTap;
import com.ellcie_healthy.ble_library.ble.profile.command.data.event.EventDataTrip;
import com.ellcie_healthy.ble_library.ble.profile.command.data.event.EventDataWarning;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import no.nordicsemi.android.ble.data.Data;

public class CommandService extends BleService<CommandEventCallback> {
    /**
     * EH Command Service UUID.
     */
    private final static UUID CONTROL_UUID_SERVICE = UUID.fromString("00ff0000-fd7a-4c87-6373-712060e11c1e");
    private static final String TAG = "CommandService";
    /**
     * CS Command characteristic UUID.
     */
    private final static UUID CONTROL_UUID_CS_COMMAND_CHAR = UUID.fromString("00ff0001-fd7a-4c87-6373-712060e11c1e");
    /**
     * CS Event characteristic UUID.
     */
    private final static UUID CONTROL_UUID_CS_EVENT_CHAR = UUID.fromString("c11644eb-530a-44cd-91cc-547137221946");
    private final CommandEventDataCallback mControlEventDataCallback = new CommandEventDataCallback() {
        @Override
        public void onEventLocalizeMyPhone(@NonNull EventDataLocalize event) {
            if (mCallbacks != null) mCallbacks.onEventLocalizeMyPhone(event);
        }

        @Override
        public void onEventTripStateChange(@NonNull EventDataTrip event) {
            if (mCallbacks != null) mCallbacks.onEventTripStateChange(event);
        }

        @Override
        public void onEventWarning(@NonNull EventDataWarning event) {
            if (mCallbacks != null) mCallbacks.onEventWarning(event);
        }

        @Override
        public void onEventTapsMode(@NonNull EventDataTap event) {
            if (mCallbacks != null) mCallbacks.onEventTapsMode(event);
        }

        @Override
        public void onEventFallStateChange(@NonNull EventDataFall event) {
            if (mCallbacks != null) mCallbacks.onEventFallStateChange(event);
        }

        @Override
        public void onEventShutdown(@NonNull EventDataShutdown event) {
            if (mCallbacks != null) mCallbacks.onEventShutdown(event);
        }

        @Override
        public void onSilentMode(@NonNull @NotNull EventDataSilentMode event) {
            if (mCallbacks != null) mCallbacks.onSilentMode(event);
        }

        @Override
        public void onUnimplementedEvent(@NonNull EventCode code, @NonNull EventData event) {
            if (mCallbacks != null) mCallbacks.onUnimplementedEvent(code, event);
        }

        @Override
        public void onInvalidDataReceived(@NonNull final BluetoothDevice device,
                                          @NonNull final Data data) {
            Log.w(TAG, "Invalid data received: " + data);
        }
    };

    public CommandService(final EHBleManager manager) {
        super(manager, CONTROL_UUID_SERVICE, "Control Service");
        final CommandCharacteristic controlChar = new CommandCharacteristic();
        this.addCharacteristic(controlChar);

        final BleCharacteristic<CommandEventDataCallback> eventChar = new BleCharacteristic<>(CONTROL_UUID_CS_EVENT_CHAR,
                "CS Event",
                false,
                BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY);
        eventChar.addCharacteristicCallback(mControlEventDataCallback);
        this.addCharacteristic(eventChar);
    }

    public BleWriteCharacteristic getCommandChar() {
        return (BleWriteCharacteristic) findCharacteristicByUuid(CONTROL_UUID_CS_COMMAND_CHAR);
    }


    @Override
    public void onReady() {
        Log.d(TAG, "onReady");
    }

    private static class CommandCharacteristic extends BleWriteCharacteristic {
        public CommandCharacteristic() {
            super(CONTROL_UUID_CS_COMMAND_CHAR, "CS Command", BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY);
        }
    }
}
