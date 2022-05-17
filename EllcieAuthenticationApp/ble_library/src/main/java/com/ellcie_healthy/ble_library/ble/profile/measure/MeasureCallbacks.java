package com.ellcie_healthy.ble_library.ble.profile.measure;

import com.ellcie_healthy.ble_library.ble.profile.measure.callback.DataGatheringCallback;
import com.ellcie_healthy.ble_library.ble.profile.measure.callback.DebugCallback;
import com.ellcie_healthy.ble_library.ble.profile.measure.callback.HumidityCallback;
import com.ellcie_healthy.ble_library.ble.profile.measure.callback.PedometerCallback;
import com.ellcie_healthy.ble_library.ble.profile.measure.callback.PressureCallback;
import com.ellcie_healthy.ble_library.ble.profile.measure.callback.RiskCallback;
import com.ellcie_healthy.ble_library.ble.profile.measure.callback.TemperatureCallback;
import com.ellcie_healthy.ble_library.ble.profile.measure.callback.WornTimeCallback;

public interface MeasureCallbacks extends DebugCallback, DataGatheringCallback, HumidityCallback, WornTimeCallback, PedometerCallback, PressureCallback,
        RiskCallback, TemperatureCallback {
}
