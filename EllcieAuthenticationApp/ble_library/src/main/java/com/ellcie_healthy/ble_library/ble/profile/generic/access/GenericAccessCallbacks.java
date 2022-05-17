package com.ellcie_healthy.ble_library.ble.profile.generic.access;

import com.ellcie_healthy.ble_library.ble.profile.generic.access.callback.AppearanceCallback;
import com.ellcie_healthy.ble_library.ble.profile.generic.access.callback.DeviceNameCallback;
import com.ellcie_healthy.ble_library.ble.profile.generic.access.callback.PreferredConnectionParametersCallback;

public interface GenericAccessCallbacks extends AppearanceCallback, DeviceNameCallback, PreferredConnectionParametersCallback {
}
