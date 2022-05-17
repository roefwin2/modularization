package com.ellcie_healthy.ble_library.ble.profile;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import androidx.annotation.NonNull;

import java.util.UUID;

public class BleCharacteristic<E extends ReadStatusCallback> {
    protected E mCallback;
    private BluetoothGattCharacteristic mCharacteristic;
    private UUID mUuid;
    private String mName;
    private int mProperties;
    private int mWantedProperties;
    private boolean mReadInit;

    public BleCharacteristic(UUID uuid, String name, boolean readInit) {
        this(uuid, name, readInit, BluetoothGattCharacteristic.PROPERTY_READ);
    }

    public BleCharacteristic(UUID uuid, String name, boolean readInit, int wantedProperties) {
        mCharacteristic = null;
        mUuid = uuid;
        mName = name;
        mReadInit = readInit;

        mWantedProperties = wantedProperties;
        mProperties = 0;
        mCallback = null;
    }

    public void addCharacteristicCallback(@NonNull E cb) {
        mCallback = cb;
    }

    public void removeCharacteristicCallback() {
        mCallback = null;
    }

    public boolean areCharacteristicsSupported(@NonNull final BluetoothGattService service) {
        mProperties = 0;

        if (service == null) {
            return false;
        }

        mCharacteristic = service.getCharacteristic(mUuid);
        if (mCharacteristic == null) {
            return false;
        }

        mProperties = mCharacteristic.getProperties();


//        Log.d("BleCharacteristic", mName + " - compare: " + mProperties + " & " + mWantedProperties + " = " + mWantedProperties);

        return ((mProperties & mWantedProperties) == mWantedProperties);
    }

    public void onDisconnect() {
        mCharacteristic = null;
        mProperties = 0;
    }

    public void onNotifyEnabled() {
        // do nothing by default;
    }

    public void onNotifyDisabled() {
        // do nothing by default;
    }

    public String getName() {
        return mName;
    }

    public UUID getUuid() {
        return mUuid;
    }

    public boolean wantReadInit() {
        return mReadInit;
    }

    public boolean wantNotify() {
        return (mWantedProperties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) == BluetoothGattCharacteristic.PROPERTY_NOTIFY;
    }

    public boolean wantIndicate() {
        return (mWantedProperties & BluetoothGattCharacteristic.PROPERTY_INDICATE) == BluetoothGattCharacteristic.PROPERTY_INDICATE;
    }

    public E getCallback() {
        return mCallback;
    }

    public BluetoothGattCharacteristic getCharacteristic() {
        return mCharacteristic;
    }

    @NonNull
    @Override
    public String toString() {
        return mName + " (" + mUuid + ")";
    }
}
