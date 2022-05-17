package com.ellcie_healthy.ble_library.ble.profile.battery;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import androidx.annotation.NonNull;

import com.ellcie_healthy.ble_library.ble.profile.BleCharacteristic;
import com.ellcie_healthy.ble_library.ble.profile.BleService;
import com.ellcie_healthy.ble_library.ble.profile.EHBleManager;
import com.ellcie_healthy.ble_library.ble.profile.battery.callback.BatteryLevelDataCallback;
import com.ellcie_healthy.ble_library.ble.profile.battery.callback.BatteryPowerStateDataCallback;

import java.util.UUID;

import no.nordicsemi.android.ble.data.Data;

public class BatteryService extends BleService<BatteryManagerCallbacks> {
    private static final String TAG = "BatteryService";

    /**
     * Battery Service UUID.
     */
    private final static UUID BATTERY_SERVICE_UUID = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb");
    /**
     * Battery Level characteristic UUID.
     */
    private final static UUID BATTERY_LEVEL_CHARACTERISTIC_UUID = UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb");
    /**
     * Battery Power State characteristic UUID.
     */
    private final static UUID BATTERY_POWER_STATE_CHARACTERISTIC_UUID = UUID.fromString("00002A1A-0000-1000-8000-00805f9b34fb");

    private Integer mBatteryLevel;
    private Boolean mIsPlugged;
    private Boolean mIsCharging;
    private BatteryLevelDataCallback mBatteryLevelDataCallback = new BatteryLevelDataCallback() {
            @Override
            public void onBatteryLevel(@NonNull final BluetoothDevice device, final int batteryLevel) {
                Log.d(TAG, "Battery Level received: " + batteryLevel + "%");
                mBatteryLevel = batteryLevel;
                mCallbacks.onBatteryLevel(device, batteryLevel);
            }

        @Override
        public void onInvalidDataReceived(@NonNull final BluetoothDevice device, final @NonNull Data data) {
            Log.w(TAG, "Invalid Battery Level data received: " + data);
        }
    };
    private BatteryPowerStateDataCallback mBatteryPowerStateDataCallback = new BatteryPowerStateDataCallback() {
        @Override
        public void onBatteryPowerState(@NonNull final BluetoothDevice device, @NonNull final Boolean plug, @NonNull final Boolean charging) {
            Log.d(TAG, "Battery Power State received: plugged " + plug + " - charging: " + charging);
            mIsPlugged = plug;
            mIsCharging = charging;
            mCallbacks.onBatteryPowerState(device, plug, charging);
        }

        @Override
        public void onInvalidDataReceived(@NonNull final BluetoothDevice device, final @NonNull Data data) {
            Log.w(TAG, "Invalid Battery Power State data received: " + data);
        }
    };

    public BatteryService(final EHBleManager manager) {
        super(manager, BATTERY_SERVICE_UUID, "Battery Service");
        final BleCharacteristic<BatteryLevelDataCallback> levelChar = new BleCharacteristic<>(BATTERY_LEVEL_CHARACTERISTIC_UUID,
                "Battery Level",
                true,
                BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY);
        levelChar.addCharacteristicCallback(mBatteryLevelDataCallback);
        this.addCharacteristic(levelChar);

        final BleCharacteristic<BatteryPowerStateDataCallback> stateChar = new BleCharacteristic<>(BATTERY_POWER_STATE_CHARACTERISTIC_UUID,
                "Battery Power State",
                true,
                BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY);
        stateChar.addCharacteristicCallback(mBatteryPowerStateDataCallback);
        this.addCharacteristic(stateChar);
    }

    /**
     * Returns the last received Battery Level value.
     * The value is set to null when the device disconnects.
     *
     * @return Battery Level value, in percent.
     */
    public Integer getBatteryLevel() {
        return mBatteryLevel;
    }

    public boolean isPowerPlug() {
        return mIsPlugged;
    }

    public boolean isBatteryCharging() {
        return mIsCharging;
    }

//    @Override
//    public String getTag() {
//        return TAG;
//    }
}
