package com.ellcie_healthy.ble_library.ble.profile.command.callback;

import androidx.annotation.NonNull;

import com.ellcie_healthy.ble_library.ble.profile.command.data.event.EventCode;
import com.ellcie_healthy.ble_library.ble.profile.command.data.event.EventData;
import com.ellcie_healthy.ble_library.ble.profile.command.data.event.EventDataFall;
import com.ellcie_healthy.ble_library.ble.profile.command.data.event.EventDataLocalize;
import com.ellcie_healthy.ble_library.ble.profile.command.data.event.EventDataShutdown;
import com.ellcie_healthy.ble_library.ble.profile.command.data.event.EventDataSilentMode;
import com.ellcie_healthy.ble_library.ble.profile.command.data.event.EventDataTap;
import com.ellcie_healthy.ble_library.ble.profile.command.data.event.EventDataTrip;
import com.ellcie_healthy.ble_library.ble.profile.command.data.event.EventDataWarning;

public interface CommandEventCallback {
    void onEventLocalizeMyPhone(@NonNull final EventDataLocalize event);

    void onEventTripStateChange(@NonNull final EventDataTrip event);

    void onEventWarning(@NonNull final EventDataWarning event);

    void onEventTapsMode(@NonNull final EventDataTap event);

    void onEventFallStateChange(@NonNull final EventDataFall event);

    void onEventShutdown(@NonNull final EventDataShutdown event);

    void onSilentMode(@NonNull final EventDataSilentMode event);

    void onUnimplementedEvent(@NonNull final EventCode code, @NonNull final EventData event);
}
