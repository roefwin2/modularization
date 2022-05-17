package com.ellcie_healthy.ble_library.ble.profile.battery;

import com.ellcie_healthy.ble_library.ble.profile.battery.callback.BatteryLevelCallback;
import com.ellcie_healthy.ble_library.ble.profile.battery.callback.BatteryPowerStateCallback;

public interface BatteryManagerCallbacks extends BatteryLevelCallback, BatteryPowerStateCallback {
}