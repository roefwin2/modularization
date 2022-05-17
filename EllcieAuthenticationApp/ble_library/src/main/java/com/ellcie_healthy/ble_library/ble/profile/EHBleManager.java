package com.ellcie_healthy.ble_library.ble.profile;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.ellcie_healthy.ble_library.ble.models.streaming.SensorDataBroker;
import com.ellcie_healthy.ble_library.ble.models.streaming.SensorDataConsumer;
import com.ellcie_healthy.ble_library.ble.profile.battery.BatteryManagerCallbacks;
import com.ellcie_healthy.ble_library.ble.profile.battery.BatteryService;
import com.ellcie_healthy.ble_library.ble.profile.command.CommandService;
import com.ellcie_healthy.ble_library.ble.profile.command.callback.CommandEventCallback;
import com.ellcie_healthy.ble_library.ble.profile.fota.FotaService;
import com.ellcie_healthy.ble_library.ble.profile.generic.access.GenericAccessService;
import com.ellcie_healthy.ble_library.ble.profile.generic.information.DeviceInformationCallbacks;
import com.ellcie_healthy.ble_library.ble.profile.generic.information.DeviceInformationService;
import com.ellcie_healthy.ble_library.ble.profile.measure.MeasureCallbacks;
import com.ellcie_healthy.ble_library.ble.profile.measure.MeasureService;
import com.ellcie_healthy.common.callbacks.EllcieCommonCallbackGetBoolean;
import com.ellcie_healthy.common.callbacks.EllcieCommonCallbackGetGeneric;
import com.ellcie_healthy.common.callbacks.EllcieCommonCommandResponseCallback;
import com.ellcie_healthy.common.callbacks.EllcieCommonOtaFinishCallback;
import com.ellcie_healthy.common.callbacks.EllcieCommonOtaProgressCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import no.nordicsemi.android.ble.ConnectionPriorityRequest;
import no.nordicsemi.android.ble.callback.DataReceivedCallback;
import no.nordicsemi.android.ble.data.Data;
import no.nordicsemi.android.ble.exception.BluetoothDisabledException;
import no.nordicsemi.android.ble.exception.DeviceDisconnectedException;
import no.nordicsemi.android.ble.exception.InvalidDataException;
import no.nordicsemi.android.ble.exception.InvalidRequestException;
import no.nordicsemi.android.ble.exception.RequestFailedException;
import no.nordicsemi.android.ble.livedata.ObservableBleManager;
import no.nordicsemi.android.ble.livedata.state.ConnectionState;

import static com.ellcie_healthy.ble_library.ble.profile.command.data.cmd.CommandCode.COMMAND_GET_LOG;

public class EHBleManager extends ObservableBleManager {

    private static final String TAG = "EHBleManager";
    private final MeasureService mMeasureService;
    private final CommandService mCommandService;
    private final DeviceInformationService mDeviceInformationService;
    private final BatteryService mBatteryService;
    private final FotaService mFotaService;
    private final ArrayList<BleService> mServices;
    private boolean mSupported;

    private BleManagerGattCallback mBleGattCallback;
    private final SensorDataBroker mSensorDataBroker;


    private DataReceivedCallback mOtaTuCallback;

    public EHBleManager(@NonNull final Context context) {
        super(context);

        mServices = new ArrayList<>();
        mSensorDataBroker = new SensorDataBroker();

//        mServices.add(new GenericAttributeService(this));
        mServices.add(new GenericAccessService(this));

        mMeasureService = new MeasureService(this,mSensorDataBroker);
        mServices.add(mMeasureService);

        mCommandService = new CommandService(this);
        mServices.add(mCommandService);

        mFotaService = new FotaService(this);
        mServices.add(mFotaService);


        mDeviceInformationService = new DeviceInformationService(this);
        mServices.add(mDeviceInformationService);

        mBatteryService = new BatteryService(this);
        mServices.add(mBatteryService);


        mSupported = false;
    }

    public EHBleManager(@NonNull final Context context,
                        @Nullable DeviceInformationCallbacks dic,
                        @Nullable BatteryManagerCallbacks bmc,
                        @Nullable CommandEventCallback cec,
                        @Nullable MeasureCallbacks mc) {
        this(context);

        mCommandService.setCallbacks(cec);
        mDeviceInformationService.setCallbacks(dic);
        mBatteryService.setCallbacks(bmc);
        mMeasureService.setCallbacks(mc);
        //mFotaService.setCallbacks(fc);
    }

    public boolean addSensorDataConsumer(@NonNull SensorDataConsumer<?> consumer) {
        return mSensorDataBroker.addConsumer(consumer);
    }

    public void delSensorDataConsumer(@NonNull SensorDataConsumer<?> consumer) {
        mSensorDataBroker.delConsumer(consumer);
    }

    public void startListeners(@NonNull SensorDataConsumer.SensorDataConsumerType type) {
        mMeasureService.clearStreamingTimestamps();
        mSensorDataBroker.startListeners(type);
    }

    public void stopListeners(@NonNull SensorDataConsumer.SensorDataConsumerType type) {
        mSensorDataBroker.stopListeners(type);
    }

    public void setDeviceInformationCallbacks(@Nullable DeviceInformationCallbacks dic) {
        mDeviceInformationService.setCallbacks(dic);
    }

    public void setBatteryManagerCallback(@Nullable BatteryManagerCallbacks bmc) {
        mBatteryService.setCallbacks(bmc);
    }

    public void setCommandEventCallback(@Nullable CommandEventCallback cec) {
        mCommandService.setCallbacks(cec);
    }

    private void requestConnectionPriorityInternal(int priority) {
        this.requestConnectionPriority(priority)
                .fail((device, status) -> Log.e(TAG, "unable to request connection priority")).enqueue();
    }

    public void requestConnectionPriorityHigh() {
        requestConnectionPriorityInternal(ConnectionPriorityRequest.CONNECTION_PRIORITY_HIGH);
    }

    public void requestConnectionPriorityBalanced() {
        requestConnectionPriorityInternal(ConnectionPriorityRequest.CONNECTION_PRIORITY_BALANCED);
    }

    @SuppressWarnings("unused")
    public void requestConnectionPriorityLow() {
        requestConnectionPriorityInternal(ConnectionPriorityRequest.CONNECTION_PRIORITY_LOW_POWER);
    }

    @NonNull
    @Override
    protected BleManagerGattCallback getGattCallback() {

        if (mBleGattCallback == null) {
            mBleGattCallback = new EHBleManagerGattCallback();
        }

        return mBleGattCallback;
    }

    public final MeasureService getMeasureService() {
        return mMeasureService;
    }

    public final DeviceInformationService getDeviceInformationService() {
        return  mDeviceInformationService;
    }

    public final FotaService getFotaService(){
        return mFotaService;
    }

    @Override
    protected boolean shouldClearCacheWhenDisconnected() {
        return !mSupported;
//        return true;
    }


    @Override
    protected ConnectionPriorityRequest requestConnectionPriority(int priority) {
        Log.d(TAG, "requestConnectionPriority: " + priority);
        return super.requestConnectionPriority(priority);
    }

    @SuppressWarnings("unchecked")
    public void initialiseService(@NonNull BleService service) {
        Log.d(TAG, "initialiseService: " + service);
        final List<BleCharacteristic> list = service.getCharacteristics();
        for (BleCharacteristic c : list) {
            if (c.wantReadInit()) {
                readCharacteristic(c);
            }

            if (c.wantNotify()) {
                enableNotifications(c);
            }

            if (c.wantIndicate()) {
                enableIndications(c);
            }
        }
    }

    public void continueOta(EllcieCommonOtaFinishCallback cbOtaCompleted, EllcieCommonOtaProgressCallback cbOtaProgress){
        getFotaService().setListener(new FotaService.IOtaStateListener() {
            @Override
            public void onOtaStarted(boolean success) {
                Log.e(TAG, "Should never happened as it a reattach workflow for ota already begun");
            }

            @Override
            public void onOtaCompleted(boolean success, int reason) {
                cbOtaCompleted.finish(success, reason);
            }

            @Override
            public void onOtaPercentageChanged(int progress) {
                cbOtaProgress.onOtaPercentageChanged(progress);
            }
        });
    }

    public void startOta(String fileDir, EllcieCommonCallbackGetBoolean cbOtaStarted, EllcieCommonOtaFinishCallback cbOtaCompleted, EllcieCommonOtaProgressCallback cbOtaProgress) {

        getFotaService().setFileDir(fileDir);
        getFotaService().setListener(new FotaService.IOtaStateListener() {
            @Override
            public void onOtaStarted(boolean success) {
                if (success) {
                    cbOtaStarted.done(true);
                } else {
                    cbOtaCompleted.finish(false, -1);
                }
            }

            @Override
            public void onOtaCompleted(boolean success, int reason) {
                cbOtaCompleted.finish(success, reason);
            }

            @Override
            public void onOtaPercentageChanged(int progress) {
                cbOtaProgress.onOtaPercentageChanged(progress);
            }
        });
        getFotaService().startOta();
    }

    public void enableNotificationForOta() {
        mOtaTuCallback = (device, data) -> {

            data.getValue();
            getFotaService().onOtaExpectedImageTuSequenceNumberReceived(Arrays.copyOf(data.getValue(), data.getValue().length));
        };
        BleCharacteristic fotaNotif = getFotaService().findCharacteristicByUuid(FotaService.FOTA_EVENT_CHARACTERISTIC_UUID);

        setNotificationCallback(fotaNotif.getCharacteristic()).with(mOtaTuCallback);
        beginAtomicRequestQueue().add(
                super.enableNotifications(fotaNotif.getCharacteristic())
        ).enqueue();
    }

    public void readOtaImage() {
        if (isConnected()) {
            final BleCharacteristic imageCharacteristic = getFotaService().findCharacteristicByUuid(FotaService.IMAGE_CHARACTERISTIC_UUID);
            beginAtomicRequestQueue().add(
                    readCharacteristic(imageCharacteristic.getCharacteristic())
                            .with((device, data) -> getFotaService().onOtaImageCharacteristicReceived(Arrays.copyOf(data.getValue(), data.getValue().length)))
            ).enqueue();
        }
    }

    public void writeOtaNewImage(byte[] byteArray, EllcieCommonCommandResponseCallback callback) {
        if (isConnected()) {
            final BleCharacteristic newImageCharacteristic = getFotaService().findCharacteristicByUuid(FotaService.NEW_IMAGE_CHARACTERISTIC_UUID);
            beginAtomicRequestQueue().add(
                    writeCharacteristic(newImageCharacteristic.getCharacteristic(), byteArray).done(device -> callback.onResponseReceived(new byte[]{0x00}))
            ).enqueue();
        }
    }

    public void writeOtaNewImageTuContent(byte[] byteArray, EllcieCommonCommandResponseCallback callback) {
        final BleCharacteristic newImageTuCharacteristic = getFotaService().findCharacteristicByUuid(FotaService.NEW_IMAGE_TU_CONTENT_CHARACTERISTIC_UUID);
        beginAtomicRequestQueue().add(
                writeCharacteristic(newImageTuCharacteristic.getCharacteristic(), byteArray, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE).done(device -> callback.onResponseReceived(new byte[]{0x00}))
        ).enqueue();

    }


    public void readCharacteristic(@NonNull BleCharacteristic characteristic) {
        if (!isConnected()) return;

        if (characteristic.getCallback() == null) return;

        readCharacteristic(characteristic.getCharacteristic())
                .with(characteristic.getCallback())
                .fail((d, s) -> Log.e(TAG, characteristic.getName() + ": unable to read: " + s))
                .done(device1 -> Log.d(TAG, characteristic.getName() + ": read retry ok"))
                .enqueue();
    }

    @SuppressWarnings("unused")
    public boolean writeCharacteristic(@NonNull BleWriteCharacteristic characteristic, @NonNull Data data) {
        return writeCharacteristic(characteristic, data, new WriteStatusCallback() {
            @Override
            public void onDataSent(@NonNull BluetoothDevice device, @NonNull Data data) {
                Log.d(TAG, "onDataSent: " + data);
            }

            @Override
            public void onRequestFailed(@NonNull BluetoothDevice device, int status) {
                Log.e(TAG, "onRequestFailed: " + status);
            }
        });
    }

    public boolean writeCharacteristic(@NonNull BleWriteCharacteristic characteristic, @NonNull Data data, @NonNull WriteStatusCallback callback) {
        if (!isConnected()) return false;
        if (data.size() < 1) return false;

        try {
            writeCharacteristic(characteristic.getCharacteristic(), data)
                    .with(callback)
                    .fail(callback)
                    .await();

            return true;
        } catch (RequestFailedException | DeviceDisconnectedException | BluetoothDisabledException | InvalidRequestException ignored) {
        }

        return false;
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean writeCharacteristicWithResponse(@NonNull BleWriteCharacteristic characteristic, @NonNull WriteRequestWithResponse request, long timeout) {
        if (!isConnected()) return false;
        Data data = request.getRequestData();
        if (data == null || data.size() < 1) return false;

//        Log.d(TAG, "Write characteristic: " + data);
        try {
//            Log.d(TAG, "send request");
            waitForNotification(characteristic.getCharacteristic())
                    .trigger(writeCharacteristic(characteristic.getCharacteristic(), data).with(request))
                    .timeout(timeout)
                    .fail(request)
                    .awaitValid(request);

            return true;
        } catch (RequestFailedException | InterruptedException | InvalidRequestException | BluetoothDisabledException | DeviceDisconnectedException ignored) {
        } catch (InvalidDataException e) {
            if (request.shouldRetry()) {
                try {
                    Log.d(TAG, "shouldRetry");
                    waitForNotification(characteristic.getCharacteristic())
                            .timeout(timeout)
                            .awaitValid(request);
                    Log.d(TAG, "retry ok");
                    return true;
                } catch (RequestFailedException | InterruptedException | InvalidRequestException | BluetoothDisabledException | DeviceDisconnectedException | InvalidDataException ignored) {
                }
            }
        }

        return false;
    }

    public void clearAndDisconnect(boolean close) {
//        Log.d(TAG, "cancel queue and disconnect");
//        cancelQueue();
//        try {
//            Thread.sleep(500);
//        } catch (InterruptedException e) {
//        }
        disconnect().enqueue();
        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {
        }

        if (close) {
            close();
        }
    }

    public void enableNotifications(@NonNull final BleCharacteristic c) {
        Log.d(TAG, "enableNotifications: " + c);

        if (!isConnected()) return;

        if (c.getCallback() != null) {
            setNotificationCallback(c.getCharacteristic()).with(c.getCallback());
        }

        Log.d(TAG, c.getName() + ": enabling notification");

        enableNotifications(c.getCharacteristic())
                .done(device -> {
                    Log.i(TAG, c.getName() + ": notifications enabled");
                    c.onNotifyEnabled();
                })
                .fail((device, status) -> {
                    Log.w(TAG, c.getName() + ": unable to enable notify");
                    clearAndDisconnect(true);
                })
                .enqueue();
    }

    public void enableIndications(@NonNull final BleCharacteristic c) {
        Log.d(TAG, "enableIndications: " + c);

        if (!isConnected()) return;

        if (c.getCallback() != null) {
            setIndicationCallback(c.getCharacteristic()).with(c.getCallback());
        }

        enableIndications(c.getCharacteristic())
                .done(device -> {
                    Log.i(TAG, c.getName() + ": indication enabled");
                    c.onNotifyEnabled();
                })
                .fail((device, status) -> {
                    Log.w(TAG, c.getName() + ": unable to enable indication");
                    clearAndDisconnect(true);
                })
                .enqueue();
    }

    public void disableNotifications(@NonNull BleCharacteristic c) {
        if (!isConnected()) return;

        disableNotifications(c.getCharacteristic())
                .done(device -> {
                    Log.i(TAG, c.getName() + ": notifications disabled");
                    c.onNotifyDisabled();
                })
                .enqueue();
    }

    public void disableIndications(@NonNull BleCharacteristic c) {
        if (!isConnected()) return;

        disableIndications(c.getCharacteristic())
                .done(device -> {
                    Log.i(TAG, c.getName() + ": indications disabled");
                    c.onNotifyDisabled();
                })
                .enqueue();
    }

    @Override
    public void log(int priority, @NonNull String message) {
        super.log(priority, message);
        message = "lib log: " + message;
        switch (priority) {
            case Log.DEBUG:
//                Log.d(TAG, message);
                break;
            case Log.INFO:
//                Log.i(TAG, message);
                break;
            case Log.VERBOSE:
//                Log.v(TAG, message);
                break;
            case Log.WARN:
                Log.w(TAG, message);
                break;
            case Log.ERROR:
            default:
                Log.e(TAG, message);
                break;
        }
    }

    @Nullable
    public final CommandService getCommandService() {
        for (BleService<?> service : mServices) {
            if (service instanceof CommandService)
                return (CommandService) service;
        }

        return null;
    }

    public void setRequestConnectionPriorityBalanced() {
        requestConnectionPriorityBalanced();
    }

    public void getDriverGlassesLog(byte seqNb, EllcieCommonCallbackGetGeneric<byte[]> cb) {

        if (getCommandService() == null) {
            return;
        }
        BleCharacteristic command_c = getCommandService().getCommandChar();
        byte[] payloadGlassesLog = new byte[]{seqNb,COMMAND_GET_LOG.getCode()};

        command_c.addCharacteristicCallback((device, data) -> cb.done(data.getValue()));
        enableNotifications(command_c);


        beginAtomicRequestQueue().add(
                writeCharacteristic(command_c.getCharacteristic(), payloadGlassesLog).invalid(() -> {

                }).done(device -> Log.d(TAG, "setCommand: done"))).enqueue();
    }

    public LiveData<ConnectionState> getState(){
        return this.state;
    }


    /**
     * BluetoothGatt callbacks object.
     */
    private class EHBleManagerGattCallback extends BleManagerGattCallback {
        @Override
        protected void initialize() {
            super.initialize();

            requestConnectionPriorityHigh();

            Log.d(TAG, "ensure bond");
            ensureBond();

            Log.d(TAG, "initialize");
            for (BleService<?> service : mServices) {
                service.initialize();
            }
        }

//        @Override
//        protected void onManagerReady() {
//            super.onManagerReady();
//            Log.d(TAG, "onManagerReady");
//        }


        @Override
        protected void onDeviceReady() {
            super.onDeviceReady();
            Log.d(TAG, "onDeviceReady");

            for (BleService service : mServices) {
                service.onReady();
            }
        }

        @Override
        public boolean isRequiredServiceSupported(@NonNull final BluetoothGatt gatt) {
            mSupported = false;

            Log.d(TAG, "isRequiredServiceSupported");

            for (BleService<?> service : mServices) {
                if (!service.isRequiredServiceSupported(gatt)) return false;
            }

            mSupported = true;
            return true;
        }

        @Override
        protected void onServicesInvalidated() {
            Log.d(TAG, "onDeviceDisconnected");
            for (BleService<?> service : mServices) {
                service.onDeviceDisconnected();
            }
        }
    }

}
