package com.ellcie_healthy.ble_library.ble.models;

import android.os.ParcelUuid;

import androidx.annotation.IntRange;
import androidx.lifecycle.LiveData;

import com.ellcie_healthy.ble_library.adapter.DiscoveredBluetoothDevice;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import no.nordicsemi.android.support.v18.scanner.ScanRecord;
import no.nordicsemi.android.support.v18.scanner.ScanResult;

public class DevicesLiveData extends LiveData<List<DiscoveredBluetoothDevice>> {
    public static final int NO_RSSI_FILTER_VALUE = Integer.MIN_VALUE;
    private static final String TAG = DevicesLiveData.class.getSimpleName();
    private final List<DiscoveredBluetoothDevice> mDevices = new ArrayList<>();
    private final List<DiscoveredBluetoothDevice> mFilteredDevices = new ArrayList<>();

    private ParcelUuid mFilterUuid;
    private int mFilterRssi;
    private String mFilterMacAddr;

    public DevicesLiveData(final UUID filterUuid, @IntRange(to = 0) final int filterRssi, final String filterMacAddr) {
        mFilterUuid = (filterUuid != null) ? new ParcelUuid(filterUuid) : null;
        mFilterRssi = filterRssi;
        mFilterMacAddr = filterMacAddr;
    }

    public DevicesLiveData() {
        mFilterUuid = null;
        mFilterRssi = NO_RSSI_FILTER_VALUE;
        mFilterMacAddr = null;
    }

    public void setFilterMacAddr(String filterMacAddr) {
        this.mFilterMacAddr = filterMacAddr;
    }

    public void setFilterRssi(@IntRange(to = 0) int filterRssi) {
        this.mFilterRssi = filterRssi;
    }

    public void setFilterUuid(UUID filterUuid) {
        this.mFilterUuid = (filterUuid != null) ? new ParcelUuid(filterUuid) : null;
    }

    public ParcelUuid getFilterUuid() {
        return this.mFilterUuid;
    }

    synchronized boolean deviceDiscovered(final ScanResult result) {
        DiscoveredBluetoothDevice device;
        boolean valid = false;

        // Check if it's a new device.
        final int index = indexOf(result);
        if (index == -1) {
            device = new DiscoveredBluetoothDevice(result);
            mDevices.add(device);
        } else {
            device = mDevices.get(index);
        }

        // Update RSSI and name.
        device.update(result);

        if (mFilteredDevices.contains(device)) {
            valid = true;
        } else if (matchesAddress(result) && matchesUuidFilter(result) && matchesNearbyFilter(device.getHighestRssi()) && result.getDevice().getName() != null && result.getDevice().getName().equals(device.getName())) {
            mFilteredDevices.add(device);
            valid = true;
        }

        if (valid) {
            postValue(mFilteredDevices);
        }

        return valid;
    }

    /**
     * Clears the list of devices.
     */
    public synchronized void clear() {
        mDevices.clear();
        mFilteredDevices.clear();
        postValue(null);
    }

    /**
     * Finds the index of existing devices on the device list.
     *
     * @param result scan result.
     * @return Index of -1 if not found.
     */
    private int indexOf(final ScanResult result) {
        int i = 0;
        for (final DiscoveredBluetoothDevice device : mDevices) {
            if (device.matches(result))
                return i;
            i++;
        }
        return -1;
    }

    private boolean matchesUuidFilter(final ScanResult result) {
        if (mFilterUuid == null)
            return true;

        final ScanRecord record = result.getScanRecord();
        if (record == null)
            return false;

        final List<ParcelUuid> uuids = record.getServiceUuids();
        if (uuids == null)
            return false;

        return uuids.contains(mFilterUuid);
    }

    private boolean matchesNearbyFilter(final int rssi) {
        return rssi >= mFilterRssi;
    }

    private boolean matchesAddress(final ScanResult result) {
        if (mFilterMacAddr == null)
            return true;

        return result.getDevice().getAddress().equals(mFilterMacAddr);
    }
}
