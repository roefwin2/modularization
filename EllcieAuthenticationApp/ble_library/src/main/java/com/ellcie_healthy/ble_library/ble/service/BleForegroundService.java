package com.ellcie_healthy.ble_library.ble.service;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LifecycleService;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.ellcie_healthy.ble_library.adapter.DiscoveredBluetoothDevice;
import com.ellcie_healthy.ble_library.ble.profile.EHBleManager;
import com.ellcie_healthy.common.ServiceConstant;

import no.nordicsemi.android.ble.callback.FailCallback;
import no.nordicsemi.android.ble.livedata.state.ConnectionState;
import no.nordicsemi.android.ble.observer.ConnectionObserver;

public abstract class BleForegroundService extends LifecycleService {
    /**
     * The parameter passed when creating the service. Must contain the address of the sensor that we want to connect to
     */
    public static final String EXTRA_DEVICE_ADDRESS = BleForegroundService.class.getPackage().getName() + ".EXTRA_DEVICE_ADDRESS";
    public static final String EXTRA_DEVICE = BleForegroundService.class.getPackage().getName() + ".EXTRA_DEVICE";
    public static final String clazzName = "ClassName";
    protected static final int COMMAND_WRITE_TIMEOUT = 4000; //ms


    private String mDeviceName = "";


    private static final String TAG = BleForegroundService.class.getSimpleName();
    private final BroadcastReceiver bluetoothStateBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);

            Log.d(TAG, "[Broadcast] Action received: " + BluetoothAdapter.ACTION_STATE_CHANGED + ", state changed to " + state2String(state));

            switch (state) {
                case BluetoothAdapter.STATE_ON:
                    onBluetoothEnabled();
                    break;
                case BluetoothAdapter.STATE_TURNING_OFF:
                case BluetoothAdapter.STATE_OFF:
                    onBluetoothDisabled();
                    break;
            }
        }

        private String state2String(final int state) {
            switch (state) {
                case BluetoothAdapter.STATE_TURNING_ON:
                    return "TURNING ON";
                case BluetoothAdapter.STATE_ON:
                    return "ON";
                case BluetoothAdapter.STATE_TURNING_OFF:
                    return "TURNING OFF";
                case BluetoothAdapter.STATE_OFF:
                    return "OFF";
                default:
                    return "UNKNOWN (" + state + ")";
            }
        }
    };
    //    private static final int CONNECT_TIMEOUT = 30000;
//    private static final int CONNECT_RETRY = 2;
//    private static final int CONNECT_GRACETIME = 1200; //ms
    protected EHBleManager mBleManager;
    protected Handler mHandler;
    protected BluetoothDevice mBluetoothDevice;
    protected boolean mIsBound;
    private boolean mIsActivityChangingConf;
    private boolean shouldAutoReconnect = true;

    private final Runnable connectRunnable = new Runnable() {

        @Override
        public void run() {
            mBleManager.connect(mBluetoothDevice)
//                    .timeout(CONNECT_TIMEOUT)
//                    .retry(CONNECT_RETRY, CONNECT_GRACETIME)
                    .useAutoConnect(shouldAutoConnect())
                    .done(d -> Log.d(TAG, "connect success"))
                    .fail((d, status) -> {
                        String reason = "";
                        switch (status) {
                            case FailCallback.REASON_DEVICE_DISCONNECTED:
                                reason = "DEVICE_DISCONNECTED";
                                break;
                            case FailCallback.REASON_DEVICE_NOT_SUPPORTED:
                                reason = "DEVICE_NOT_SUPPORTED";
                                break;
                            case FailCallback.REASON_NULL_ATTRIBUTE:
                                reason = "NULL_ATTRIBUTE";
                                break;
                            case FailCallback.REASON_REQUEST_FAILED:
                                reason = "REQUEST_FAILED";
                                break;
                            case FailCallback.REASON_TIMEOUT:
                                reason = "TIMEOUT";
                                break;
                            case FailCallback.REASON_VALIDATION:
                                reason = "VALIDATION";
                                break;
                            case FailCallback.REASON_CANCELLED:
                                reason = "CANCELLED";
                                break;
                            case FailCallback.REASON_BLUETOOTH_DISABLED:
                                reason = "BLUETOOTH_DISABLED";
                                break;
                            default:
                                reason = "UNKNOWN";
                                break;
                        }
                        Log.e(TAG, "connect error: " + status + " - " + reason);

                        if (shouldAutoReconnect) {
//                            if (mBleManager != null) {
//                                mBleManager.clearAndDisconnect(true);
//                            }

                            if (mHandler != null) {
                                mHandler.removeCallbacks(connectRunnable);
                                mHandler.postDelayed(connectRunnable, 5000);
                            }
                        }
                    }).enqueue();
        }
    };
    private final Observer<ConnectionState> mConnectionObserver = state -> {
        switch (state.getState()) {
            case CONNECTING:
                onDeviceConnecting();
                break;
            case INITIALIZING:
                onDeviceInitializing();
                break;
            case READY:
                onDeviceConnected(true);
                break;
            case DISCONNECTED:
                int reason = ConnectionObserver.REASON_TERMINATE_LOCAL_HOST;
                if (state instanceof ConnectionState.Disconnected) {
                    final ConnectionState.Disconnected stateWithReason = (ConnectionState.Disconnected) state;
                    reason = stateWithReason.getReason();
                }

                onDeviceDisconnected(reason);
                break;
            case DISCONNECTING:
                onDeviceDisconnecting();
                break;
        }
    };

    /**
     * Returns a handler that is created in onCreate().
     * The handler may be used to postpone execution of some operations or to run them in UI thread.
     */
    protected Handler getHandler() {
        return mHandler;
    }

    /**
     * Returns the binder implementation. This must return class implementing the additional manager interface that may be used in the bound activity.
     *
     * @return the service binder
     */
    protected LocalBinder getBinder() {
        // default implementation returns the basic binder. You can overwrite the LocalBinder with your own, wider implementation
        return new LocalBinder();
    }

    @Override
    public IBinder onBind(final Intent intent) {
        super.onBind(intent);
        mIsBound = true;
        return getBinder();
    }


    @Override
    public final void onRebind(final Intent intent) {
        mIsBound = true;

        if (!mIsActivityChangingConf)
            onServiceRebind(intent);
    }

    protected abstract void onServiceRebind(final Intent intent);

    @Override
    public final boolean onUnbind(final Intent intent) {
        mIsBound = false;

        if (!mIsActivityChangingConf)
            onServiceUnbind(intent);

        // We want the onRebind method be called if anything else binds to it again
        return true;
    }

    /**
     * Called when the activity has unbound from the service before being finished.
     * This method is not called when the activity is killed to be recreated when the phone orientation changed.
     */
    protected abstract void onServiceUnbind(final Intent intent);

    @Override
    public void onCreate() {
        super.onCreate();

        mHandler = new Handler(Looper.getMainLooper());

        // Initialize the manager
        mBleManager = initializeManager();
        mBleManager.getState().observe(this, mConnectionObserver);


        // Register broadcast receivers
        registerReceiver(bluetoothStateBroadcastReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

        // Service has now been created
        onServiceCreated();

        // Call onBluetoothEnabled if Bluetooth enabled
        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter.isEnabled()) {
            onBluetoothEnabled();
        }
    }

    protected abstract EHBleManager initializeManager();

    /**
     * Called when the service has been created, before the {@link #onBluetoothEnabled()} is called.
     */
    protected void onServiceCreated() {
        // empty default implementation
    }

    /**
     * This method returns whether autoConnect option should be used.
     *
     * @return true to use autoConnect feature, false (default) otherwise.
     */
    protected boolean shouldAutoConnect() {
        return false;
    }

    private void doConnect() {
        shouldAutoReconnect = true;

        if (mBluetoothDevice == null) {
            return;
        }

        mHandler.removeCallbacks(connectRunnable);
        mHandler.postDelayed(connectRunnable, 1500);
    }

    public void onKill(Context context) {
        // Unregister broadcast receivers
        mHandler.removeCallbacks(connectRunnable);
        unregisterReceiver(bluetoothStateBroadcastReceiver);

        // shutdown the manager
        mBleManager.close();
        Log.d(TAG, "Service destroyed");
        mBleManager = null;
        mBluetoothDevice = null;
        mHandler = null;
    }


    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.d(TAG, "onStartCommand");
        if (intent == null || !intent.hasExtra(EXTRA_DEVICE_ADDRESS)) {
            throw new UnsupportedOperationException("No device key");
        }
        if (!intent.hasExtra(clazzName)) {
            throw new UnsupportedOperationException("No clazz name found");
        }
        Log.d(TAG, "Service started");


        final DiscoveredBluetoothDevice discoveredBluetoothDevice = intent.getParcelableExtra(EXTRA_DEVICE);
        final String deviceAddress = intent.getStringExtra(EXTRA_DEVICE_ADDRESS);


        if (discoveredBluetoothDevice != null) {
            mBluetoothDevice = discoveredBluetoothDevice.getDevice();
            mDeviceName = discoveredBluetoothDevice.getName();
            if (!deviceAddress.equals(discoveredBluetoothDevice.getAddress())) {
                throw new UnsupportedOperationException("data consistency error");
            }
        }

        if (deviceAddress.equals(ServiceConstant.DEFAULT_MAC_ADDRESS_NO_GLASSES)) { //This case is used for Fall without connected glasses : we need the service to  manage sos from application
            return START_STICKY;
        }


        if (mBluetoothDevice == null) {
            final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            Log.d(TAG, "find device by address");
            mBluetoothDevice = adapter.getRemoteDevice(deviceAddress);
        }

        Log.d(TAG, "perform connection to: " + mBluetoothDevice.getAddress());

        onServiceStarted();

        if (mBleManager.getState() != null && mBleManager.getState().getValue() == null) {
            switch (mBleManager.getState().getValue().getState()) {
                case CONNECTING:
                case INITIALIZING:
                case READY: {
                    if (mBleManager.getBluetoothDevice() != null) {
                        Log.d(TAG, "onStartCommand: " + mBleManager.getBluetoothDevice().getAddress() + " - " + deviceAddress);
                        if (mBleManager.getBluetoothDevice().getAddress() != null && mBleManager.getBluetoothDevice().getAddress().equals(deviceAddress)) {
                            Log.d(TAG, "Device already connected or connecting");
                            return START_STICKY;
                        }
                    }

                    Log.d(TAG, "Device already connected to another device: disconnect first");
                    mBleManager.clearAndDisconnect(true);
                    break;
                }
                case DISCONNECTING:
                case DISCONNECTED:
                    break;
            }
        }

        doConnect();

        return START_STICKY;
    }

    /**
     * Called when the service has been started. The device name and address are set.
     * The BLE Manager will try to connect to the device after this method finishes.
     */
    protected void onServiceStarted() {
        // empty default implementation
    }

    @Override
    public void onTaskRemoved(final Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        // This method is called when user removed the app from Recents.
        // By default, the service will be killed and recreated immediately after that.
        // However, all managed devices will be lost and devices will be disconnected.
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unregister broadcast receivers
        mHandler.removeCallbacks(connectRunnable);
        unregisterReceiver(bluetoothStateBroadcastReceiver);

        // shutdown the manager
        mBleManager.close();
        Log.d(TAG, "Service destroyed");
        mBleManager = null;
        mBluetoothDevice = null;
        mHandler = null;
    }

    /**
     * Method called when Bluetooth Adapter has been disabled.
     */
    protected void onBluetoothDisabled() {
        // empty default implementation
    }

    /**
     * This method is called when Bluetooth Adapter has been enabled and
     * after the service was created if Bluetooth Adapter was enabled at that moment.
     * This method could initialize all Bluetooth related features, for example open the GATT server.
     */
    protected void onBluetoothEnabled() {
        // empty default implementation
    }

    /**
     * This method should return false if the service needs to do some asynchronous work after if has disconnected from the device.
     * In that case the {@link #stopService()} method must be called when done.
     *
     * @return true (default) to automatically stop the service when device is disconnected. False otherwise.
     */
    protected boolean stopWhenDisconnected() {
        return true;
    }

    protected void onDeviceConnecting() {
        Log.d(TAG, "connecting");
    }

    protected void onDeviceInitializing() {
        Log.d(TAG, "initializing");
    }

    protected void onDeviceConnected(boolean forceConfig) {
        Log.d(TAG, "onConnected");
    }

    protected void onDeviceDisconnected(int reason) {
        Log.d(TAG, "disconnected: " + reason);
        if (stopWhenDisconnected()) {
            stopService();
            return;
        }

        if (!shouldAutoReconnect) {
            Log.d(TAG, "Do not reconnect");
            return;
        }

        switch (reason) {
            case ConnectionObserver.REASON_LINK_LOSS:
            case ConnectionObserver.REASON_TIMEOUT:
            case ConnectionObserver.REASON_UNKNOWN:
            case ConnectionObserver.REASON_TERMINATE_LOCAL_HOST:
                Log.d(TAG, "Try to reconnect");
                doConnect();
                break;
            case ConnectionObserver.REASON_TERMINATE_PEER_USER:
                Log.d(TAG, "stop requested by peer");
                stopService();
                break;
            case ConnectionObserver.REASON_SUCCESS:
            case ConnectionObserver.REASON_NOT_SUPPORTED:
            default:
                Log.d(TAG, "Do nothing");
                break;
        }
    }

    protected void onDeviceDisconnecting() {
        Log.d(TAG, "disconnecting");
    }

    protected void stopService() {
        // user requested disconnection. We must stop the service
        Log.d(TAG, "Stopping service...");
        stopSelf();
    }

    protected BluetoothDevice getBluetoothDevice() {
        return mBluetoothDevice;
    }

    protected String getName() {
        return mDeviceName.isEmpty() ? mBluetoothDevice == null ? null : mBluetoothDevice.getName() : mDeviceName;
    }

    protected boolean isConnected() {
        return mBleManager != null && mBleManager.isConnected();
    }

    // IBleRemote test;


    public class LocalBinder extends Binder {

        public LocalBinder getService() {
            return this;
        }

        /**
         * Disconnects from the sensor.
         */
        public final void disconnect() {
            shouldAutoReconnect = false;
            final int state = mBleManager.getConnectionState();
            if (state == BluetoothGatt.STATE_DISCONNECTED || state == BluetoothGatt.STATE_DISCONNECTING) {
                onDeviceDisconnected(ConnectionObserver.REASON_SUCCESS);
                return;
            }

            mBleManager.clearAndDisconnect(true);
        }

        /**
         * Sets whether the bound activity if changing configuration or not.
         * If <code>false</code>, we will turn off battery level notifications in onUnbind(..) method below.
         *
         * @param changing true if the bound activity is finishing
         */
        public void setActivityIsChangingConfiguration(final boolean changing) {
            mIsActivityChangingConf = changing;
        }

        /**
         * Returns the device address
         *
         * @return device address
         */
        public String getDeviceAddress() {
            return mBluetoothDevice != null ? mBluetoothDevice.getAddress() : null;
        }

        /**
         * Returns the Bluetooth device
         *
         * @return the Bluetooth device
         */
        public BluetoothDevice getBluetoothDevice() {
            return mBluetoothDevice;
        }

        /**
         * Returns <code>true</code> if the device is connected to the sensor.
         *
         * @return <code>true</code> if device is connected to the sensor, <code>false</code> otherwise
         */
        public boolean isConnected() {
            return mBleManager != null && mBleManager.isConnected();
        }

        public boolean isBonded() {
            if (mBluetoothDevice == null)
                return false;
            return mBluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDED;
        }


        public LiveData<ConnectionState> getConnectionState() {
            return mBleManager.getState();
        }

        protected String getDeviceName() {
            return getName();
        }
    }
}
