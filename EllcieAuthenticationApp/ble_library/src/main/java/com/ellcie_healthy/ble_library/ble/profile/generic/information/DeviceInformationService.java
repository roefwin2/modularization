package com.ellcie_healthy.ble_library.ble.profile.generic.information;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import androidx.annotation.NonNull;

import com.ellcie_healthy.ble_library.ble.profile.BleCharacteristic;
import com.ellcie_healthy.ble_library.ble.profile.BleService;
import com.ellcie_healthy.ble_library.ble.profile.EHBleManager;
import com.ellcie_healthy.ble_library.ble.profile.generic.information.callback.FirmwareRevisionDataCallback;
import com.ellcie_healthy.ble_library.ble.profile.generic.information.callback.ManufacturerNameDataCallback;
import com.ellcie_healthy.ble_library.ble.profile.generic.information.callback.ModelNumberDataCallback;
import com.ellcie_healthy.ble_library.ble.profile.generic.information.callback.SerialNumberDataCallback;

import java.util.UUID;

import no.nordicsemi.android.ble.data.Data;

public class DeviceInformationService extends BleService<DeviceInformationCallbacks> {
    private static final String TAG = "DeviceInformationSrv";

    /**
     * Device Information Service UUID.
     */
    private final static UUID GENERIC_INFO_SERVICE_UUID = UUID.fromString("0000180A-0000-1000-8000-00805f9b34fb");
    private final static UUID MANUFACTURER_NAME_CHARACTERISTIC_UUID = UUID.fromString("00002A29-0000-1000-8000-00805f9b34fb");
    private final static UUID MODEL_NUMBER_CHARACTERISTIC_UUID = UUID.fromString("00002A24-0000-1000-8000-00805f9b34fb");
    private final static UUID SERIAL_NUMBER_CHARACTERISTIC_UUID = UUID.fromString("00002A25-0000-1000-8000-00805f9b34fb");
    private final static UUID FIRMWARE_REVISION_CHARACTERISTIC_UUID = UUID.fromString("00002A26-0000-1000-8000-00805f9b34fb");
    private ManufacturerNameDataCallback mManufacturerNameDataCallback = new ManufacturerNameDataCallback() {
        @Override
        public void onManufacturerName(@NonNull BluetoothDevice device, @NonNull String name) {
            Log.d(TAG, "onManufacturerName: " + name);
        }
    };
    private ModelNumberDataCallback mModelNumberDataCallback = new ModelNumberDataCallback() {
        @Override
        public void onModelNumber(@NonNull BluetoothDevice device, @NonNull String model) {
            Log.d(TAG, "onModelNumber: " + model);
            if (mCallbacks != null) mCallbacks.onModelNumber(device, model);
        }

        @Override
        public void onRequestFailed(@NonNull BluetoothDevice device, int status) {
            Log.e(TAG, "unable to read model!");
            if (mCallbacks != null)
                mCallbacks.onUnexpectedError(device, status, "unable to read model");
        }
    };
    private SerialNumberDataCallback mSerialDataCallback = new SerialNumberDataCallback() {
        @Override
        public void onSerialNumber(@NonNull BluetoothDevice device, @NonNull String serial) {
            Log.d(TAG, "onSerialNumber: " + serial);
            if (mCallbacks != null) mCallbacks.onSerialNumber(device, serial);
        }

        @Override
        public void onRequestFailed(@NonNull BluetoothDevice device, int status) {
            Log.e(TAG, "unable to read serial number!");
            if (mCallbacks != null)
                mCallbacks.onUnexpectedError(device, status, "unable to read serial");
        }
    };
    private FirmwareRevisionDataCallback mFirmwareDataCallback = new FirmwareRevisionDataCallback() {
        @Override
        public void onFirmwareRevision(@NonNull BluetoothDevice device, @NonNull String firmware) {
            Log.d(TAG, "onFirmwareRevision: " + firmware);
            if (mCallbacks != null) mCallbacks.onFirmwareRevision(device, firmware);
        }

        @Override
        public void onRequestFailed(@NonNull BluetoothDevice device, int status) {
            Log.e(TAG, "unable to read firmware revision!");
            if (mCallbacks != null)
                mCallbacks.onUnexpectedError(device, status, "unable to read firmware");
        }

        @Override
        public void onInvalidDataReceived(@NonNull BluetoothDevice device, @NonNull Data data) {
            Log.e(TAG, "unable to read firmware revision!");
        }
    };

    public DeviceInformationService(final EHBleManager manager) {
        super(manager, GENERIC_INFO_SERVICE_UUID, "Device Information");
        final BleCharacteristic<ManufacturerNameDataCallback> manufacturer = new BleCharacteristic<>(MANUFACTURER_NAME_CHARACTERISTIC_UUID,
                "Manufacturer Name",
                false);
        manufacturer.addCharacteristicCallback(mManufacturerNameDataCallback);
        this.addCharacteristic(manufacturer);

        final BleCharacteristic<ModelNumberDataCallback> model = new BleCharacteristic<>(MODEL_NUMBER_CHARACTERISTIC_UUID,
                "Model Number",
                true);
        model.addCharacteristicCallback(mModelNumberDataCallback);
        this.addCharacteristic(model);

        final BleCharacteristic<SerialNumberDataCallback> serial = new BleCharacteristic<>(SERIAL_NUMBER_CHARACTERISTIC_UUID,
                "Serial Number",
                true);
        serial.addCharacteristicCallback(mSerialDataCallback);
        this.addCharacteristic(serial);

        final BleCharacteristic<FirmwareRevisionDataCallback> firmware = new BleCharacteristic<>(FIRMWARE_REVISION_CHARACTERISTIC_UUID,
                "Firmware Revision",
                true);
        firmware.addCharacteristicCallback(mFirmwareDataCallback);
        this.addCharacteristic(firmware);

    }
}
