package com.ellcie_healthy.ble_library.ble.profile.generic.access;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import androidx.annotation.NonNull;

import com.ellcie_healthy.ble_library.ble.profile.BleCharacteristic;
import com.ellcie_healthy.ble_library.ble.profile.BleService;
import com.ellcie_healthy.ble_library.ble.profile.EHBleManager;
import com.ellcie_healthy.ble_library.ble.profile.generic.access.callback.AppearanceDataCallback;
import com.ellcie_healthy.ble_library.ble.profile.generic.access.callback.DeviceNameDataCallback;
import com.ellcie_healthy.ble_library.ble.profile.generic.access.callback.PreferredConnectionParametersDataCallback;
import com.ellcie_healthy.ble_library.ble.profile.generic.access.data.BleDeviceAppearance;

import java.util.UUID;

import no.nordicsemi.android.ble.data.Data;

public class GenericAccessService extends BleService<GenericAccessCallbacks> {
    private static final String TAG = "GenericAccessService";

    /**
     * Generic Access Service UUID.
     */
    private final static UUID GENERIC_ACCESS_SERVICE_UUID = UUID.fromString("00001800-0000-1000-8000-00805f9b34fb");
    private final static UUID DEVICE_NAME_CHARACTERISTIC_UUID = UUID.fromString("00002A00-0000-1000-8000-00805f9b34fb");
    private final static UUID APPEARANCE_CHARACTERISTIC_UUID = UUID.fromString("00002A01-0000-1000-8000-00805f9b34fb");
    private final static UUID PREFERRED_CONNECTION_PARAMETERS_CHARACTERISTIC_UUID = UUID.fromString("00002A04-0000-1000-8000-00805f9b34fb");
    private DeviceNameDataCallback mDeviceNameDataCallback = new DeviceNameDataCallback() {
        @Override
        public void onDeviceName(@NonNull BluetoothDevice device, @NonNull String name) {
            Log.d(TAG, "onDeviceName: " + name);
//            mCallbacks.onDeviceName(device, name);
        }
    };
    private AppearanceDataCallback mAppearanceDataCallback = new AppearanceDataCallback() {
        @Override
        public void onAppearance(@NonNull BluetoothDevice device, @NonNull BleDeviceAppearance appearance) {
            Log.d(TAG, "onAppearance: " + appearance);
//            mCallbacks.onAppearance(device, appearance);
        }
    };
    private PreferredConnectionParametersDataCallback mPreferredConParameters = new PreferredConnectionParametersDataCallback() {
        @Override
        public void onPreferedConnectionParameters(@NonNull BluetoothDevice device, @NonNull Data data) {
            Log.d(TAG, "onPreferedConnectionParameters: " + data);
//            mCallbacks.onPreferedConnectionParameters(device, data);
        }
    };

    public GenericAccessService(final EHBleManager manager) {
        super(manager, GENERIC_ACCESS_SERVICE_UUID, "Generic Access");
        final BleCharacteristic<DeviceNameDataCallback> deviceName = new BleCharacteristic<>(DEVICE_NAME_CHARACTERISTIC_UUID,
                "Device name",
                true);
        deviceName.addCharacteristicCallback(mDeviceNameDataCallback);
        this.addCharacteristic(deviceName);

        final BleCharacteristic<AppearanceDataCallback> appearance = new BleCharacteristic<>(APPEARANCE_CHARACTERISTIC_UUID,
                "Appearance",
                false);
        appearance.addCharacteristicCallback(mAppearanceDataCallback);
        this.addCharacteristic(appearance);

        final BleCharacteristic<PreferredConnectionParametersDataCallback> preferredConParameters = new BleCharacteristic<>(PREFERRED_CONNECTION_PARAMETERS_CHARACTERISTIC_UUID,
                "Peripheral Preferred Connection Parameters",
                false);
        preferredConParameters.addCharacteristicCallback(mPreferredConParameters);
        this.addCharacteristic(preferredConParameters);

    }
}
