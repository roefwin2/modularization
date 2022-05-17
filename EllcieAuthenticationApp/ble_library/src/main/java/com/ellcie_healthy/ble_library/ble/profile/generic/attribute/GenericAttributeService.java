package com.ellcie_healthy.ble_library.ble.profile.generic.attribute;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import androidx.annotation.NonNull;

import com.ellcie_healthy.ble_library.ble.profile.BleCharacteristic;
import com.ellcie_healthy.ble_library.ble.profile.BleService;
import com.ellcie_healthy.ble_library.ble.profile.EHBleManager;
import com.ellcie_healthy.ble_library.ble.profile.generic.attribute.callback.ServiceChangedDataCallback;

import java.util.UUID;

import no.nordicsemi.android.ble.data.Data;

public class GenericAttributeService extends BleService<GenericAttributeCallbacks> {
    private static final String TAG = "GenericAttributeService";

    /**
     * Generic Attribute Service UUID.
     */
    private final static UUID GENERIC_ATTRIBUTE_SERVICE_UUID = UUID.fromString("00001801-0000-1000-8000-00805f9b34fb");
    private final static UUID SERVICE_CHANGED_CHARACTERISTIC_UUID = UUID.fromString("00002A05-0000-1000-8000-00805f9b34fb");
    private ServiceChangedDataCallback mServiceChangedDataCallback = new ServiceChangedDataCallback() {
        @Override
        public void onServiceChanged(@NonNull BluetoothDevice device, @NonNull Data data) {
            Log.d(TAG, "onServiceChanged: " + data);
//            mCallbacks.onServiceChanged(device, name);
        }
    };

    public GenericAttributeService(final EHBleManager manager) {
        super(manager, GENERIC_ATTRIBUTE_SERVICE_UUID, "Generic Attribute");
        final BleCharacteristic<ServiceChangedDataCallback> serviceChanged = new BleCharacteristic<>(SERVICE_CHANGED_CHARACTERISTIC_UUID,
                "Service Changed",
                false,
                BluetoothGattCharacteristic.PROPERTY_INDICATE);
        serviceChanged.addCharacteristicCallback(mServiceChangedDataCallback);
        this.addCharacteristic(serviceChanged);
    }
}
