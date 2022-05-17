package com.ellcie_healthy.ble_library.ble.models;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.ellcie_healthy.ble_library.ble.utils.Utils;

import java.util.List;

import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

public class ScannerViewModel extends AndroidViewModel {
    private static final String TAG = ScannerViewModel.class.getSimpleName();

    private final static long STOP_SCAN_DELAY = 5000;

    private final DevicesLiveData mDevicesLiveData;
    private final ScannerStateLiveData mScannerStateLiveData;
    private final Handler mHandler;
    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(final int callbackType, @NonNull final ScanResult result) {
            // This callback will be called only if the scan report delay is not set or is set to 0.

//            Log.d(TAG, "onScanResult");

//            Log.d(TAG, "res: " + mDevicesLiveData.deviceDiscovered(result) + " - " + result.getDevice() + " - " + result.getDevice().getName());

            // If the packet has been obtained while Location was disabled, mark Location as not required
            if (Utils.isLocationRequired() && !Utils.isLocationEnabled(getApplication()))
                Utils.markLocationNotRequired();

            if (mDevicesLiveData.deviceDiscovered(result)) {
                mScannerStateLiveData.recordFound();
            }
        }

        @Override
        public void onBatchScanResults(@NonNull final List<ScanResult> results) {
            // This callback will be called only if the report delay set above is greater then 0.
            // This callback will be called only if the report delay set above is greater then 0.

            // If the packet has been obtained while Location was disabled, mark Location as not required
            if (Utils.isLocationRequired() && !Utils.isLocationEnabled(getApplication()))
                Utils.markLocationNotRequired();

//            Log.d(TAG, "onBatchScanResults");

            boolean atLeastOneMatchedFilter = false;
            for (final ScanResult result : results) {
                atLeastOneMatchedFilter = mDevicesLiveData.deviceDiscovered(result) || atLeastOneMatchedFilter;
//                Log.d(TAG, "res: " + mDevicesLiveData.deviceDiscovered(result) + " - " + result.getDevice() + " - " + result.getDevice().getName());
            }
            if (atLeastOneMatchedFilter) {
                mScannerStateLiveData.recordFound();
            }
        }

        @Override
        public void onScanFailed(final int errorCode) {
            Log.e(TAG, "onScanFailed: " + errorCode);
            mScannerStateLiveData.clearRecords();
            switch (errorCode) {
                case ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED:
                    Log.d(TAG, "restart scan");
                    mHandler.postDelayed(() -> {
                        stopScanInternal();
                        startScan();
                    }, 100);
                    break;
                case ScanCallback.SCAN_FAILED_ALREADY_STARTED:
                    break;
                case ScanCallback.SCAN_FAILED_INTERNAL_ERROR:
                case ScanCallback.SCAN_FAILED_FEATURE_UNSUPPORTED:
                case ScanCallback.SCAN_FAILED_OUT_OF_HARDWARE_RESOURCES:
                case ScanCallback.SCAN_FAILED_SCANNING_TOO_FREQUENTLY:
                default:
                    Log.e(TAG, "stop scan");
                    stopScanInternal();
                    break;
            }
        }
    };
    private final Runnable mStopScanTask = () -> stopScanInternal();
    private final BroadcastReceiver mLocationProviderChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final boolean enabled = Utils.isLocationEnabled(context);
            mScannerStateLiveData.setLocationEnabled(enabled);
        }
    };
    private final BroadcastReceiver mBluetoothStateBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
            final int previousState = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_STATE, BluetoothAdapter.STATE_OFF);

            Log.d(TAG, "mBluetoothStateBroadcastReceiver: " + state);

            switch (state) {
                case BluetoothAdapter.STATE_ON:
                    mScannerStateLiveData.bluetoothEnabled();
                    break;
                case BluetoothAdapter.STATE_TURNING_OFF:
                case BluetoothAdapter.STATE_OFF:
                    if (previousState != BluetoothAdapter.STATE_TURNING_OFF && previousState != BluetoothAdapter.STATE_OFF) {
                        stopScan(true);
                        mScannerStateLiveData.bluetoothDisabled();
                    }
                    break;
            }
        }
    };
    private final BroadcastReceiver mDeviceDiscoverReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // nothing to do

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.v(TAG, "Discovery finished");
                final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                adapter.startDiscovery();

            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Log.v(TAG, "Discovery started");
            }
        }
    };

    public ScannerViewModel(final Application application) {
        super(application);

        mHandler = new Handler(Looper.getMainLooper());

        mScannerStateLiveData = new ScannerStateLiveData(Utils.isBleEnabled(),
                Utils.isLocationEnabled(application));
        mDevicesLiveData = new DevicesLiveData();
        registerBroadcastReceivers(application);
    }

    public DevicesLiveData getDevices() {
        return mDevicesLiveData;
    }

    public ScannerStateLiveData getScannerState() {
        return mScannerStateLiveData;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        mScannerStateLiveData.clearRecords();
        mDevicesLiveData.clear();
        getApplication().unregisterReceiver(mBluetoothStateBroadcastReceiver);

        getApplication().unregisterReceiver(mDeviceDiscoverReceiver);

        if (Utils.isMarshmallowOrAbove()) {
            getApplication().unregisterReceiver(mLocationProviderChangedReceiver);
        }
    }

    public void refresh() {
        mScannerStateLiveData.clearRecords();
        mDevicesLiveData.clear();
        mScannerStateLiveData.refresh();
    }

    public void startScan() {
        if (mScannerStateLiveData.isScanning()) {
            return;
        }

        Log.d(TAG, "startScan");

        mHandler.removeCallbacks(mStopScanTask);

        Utils.markLocationRequired();

        final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        int reportDelay = 0; // use report delay to 0 because some devices doesn't support other delay
        if (adapter != null && adapter.isOffloadedScanBatchingSupported()) {
            reportDelay = 2000;
        }

        // Scanning settings
        final ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
                .setNumOfMatches(ScanSettings.MATCH_NUM_FEW_ADVERTISEMENT)
                .setReportDelay(reportDelay)
                // Hardware filtering has some issues on selected devices
                .setUseHardwareCallbackTypesIfSupported(true)
                // Samsung S6 and S6 Edge report equal value of RSSI for all devices. In this app we ignore the RSSI.
                .setUseHardwareBatchingIfSupported(true)
                .setUseHardwareFilteringIfSupported(true)
                .setLegacy(true)
                .build();

        if (adapter.isDiscovering()) {
            adapter.cancelDiscovery();
        }

        if (!adapter.startDiscovery()) {
            Log.e(TAG, "unable to startDiscovery");
        }

        final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
        scanner.startScan(null, settings, scanCallback);
        mScannerStateLiveData.scanningStarted();
    }

    public void stopScan(boolean immediate) {
        Log.d(TAG, "stopScan: immediate " + immediate);
        mHandler.removeCallbacks(mStopScanTask);

        final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter.isDiscovering()) {
            adapter.cancelDiscovery();
        }

        if (immediate) {
            stopScanInternal();
        } else {
            mHandler.postDelayed(mStopScanTask, STOP_SCAN_DELAY);
        }
    }

    private void stopScanInternal() {
        Log.d(TAG, "stopScan");
        mHandler.removeCallbacks(mStopScanTask);

        final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter.isEnabled() && adapter.isDiscovering()) {
            adapter.cancelDiscovery();
        }

        if (mScannerStateLiveData.isScanning() && mScannerStateLiveData.isBluetoothEnabled()) {
            Log.d(TAG, "stopping scan");
            final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
            scanner.flushPendingScanResults(scanCallback);
            scanner.stopScan(scanCallback);
            Log.d(TAG, "stopped");
            mScannerStateLiveData.scanningStopped();
            Log.d(TAG, "stop live data");
        }
    }

    private void registerBroadcastReceivers(final Application application) {
        application.registerReceiver(mBluetoothStateBroadcastReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        application.registerReceiver(mDeviceDiscoverReceiver, filter);

        if (Utils.isMarshmallowOrAbove()) {
            application.registerReceiver(mLocationProviderChangedReceiver, new IntentFilter(LocationManager.MODE_CHANGED_ACTION));
        }
    }
}
