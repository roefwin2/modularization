package com.ellcie_healthy.ble_library.ble.profile.generic.information;

import com.ellcie_healthy.ble_library.ble.profile.UnexpectedErrorCallback;
import com.ellcie_healthy.ble_library.ble.profile.generic.information.callback.FirmwareRevisionCallback;
import com.ellcie_healthy.ble_library.ble.profile.generic.information.callback.ModelNumberCallback;
import com.ellcie_healthy.ble_library.ble.profile.generic.information.callback.SerialNumberCallback;

public interface DeviceInformationCallbacks extends UnexpectedErrorCallback, FirmwareRevisionCallback, ModelNumberCallback, SerialNumberCallback {
}
