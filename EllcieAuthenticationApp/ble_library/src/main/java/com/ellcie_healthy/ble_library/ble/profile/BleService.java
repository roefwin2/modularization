package com.ellcie_healthy.ble_library.ble.profile;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattService;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class BleService<E> {
    protected final EHBleManager mManager;
    private final UUID mUuid;
    private final String mName;
    protected E mCallbacks;
    private BluetoothGattService mService;
    private ArrayList<BleCharacteristic> mCharacteristics;

    public BleService(@NonNull final EHBleManager manager, @NonNull UUID uuid, @NonNull String name) {
        mUuid = uuid;
        mName = name;

        mService = null;
        mCharacteristics = new ArrayList<>();

        mManager = manager;
        mCallbacks = null;
    }

    protected void addCharacteristic(BleCharacteristic characteristic) {
        mCharacteristics.add(characteristic);
    }

    protected boolean removeCharacteristic(BleCharacteristic characteristic) {
        return mCharacteristics.remove(characteristic);
    }

    public void initialize() {
        mManager.initialiseService(this);
    }

    public void onReady() {
        // do nothing by default;
    }

    public void setCallbacks(@NonNull final E callbacks) {
        mCallbacks = callbacks;
    }

    public boolean isRequiredServiceSupported(@NonNull final BluetoothGatt gatt) {
        mService = gatt.getService(mUuid);
        if (mService == null) return false;

        for (BleCharacteristic c : mCharacteristics) {
            if (!c.areCharacteristicsSupported(mService)) return false;
        }

        return true;
    }

    public void onDeviceDisconnected() {
        mService = null;
        for (BleCharacteristic c : mCharacteristics) {
            c.onDisconnect();
        }
    }

    public List<BleCharacteristic> getCharacteristics() {
        return mCharacteristics;
    }

    public BleCharacteristic findCharacteristicByUuid(UUID uuid) {
        for (BleCharacteristic c : mCharacteristics) {
            if (c.getUuid().equals(uuid)) return c;
        }

        return null;
    }

    public final String getName() {
        return mName;
    }

    public final UUID getUuid() {
        return mUuid;
    }

    @NonNull
    @Override
    public String toString() {
        return mName + " (" + mUuid + ")";
    }
}
