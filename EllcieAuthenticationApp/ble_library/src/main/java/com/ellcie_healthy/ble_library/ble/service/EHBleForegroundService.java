package com.ellcie_healthy.ble_library.ble.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.ellcie_healthy.ble_library.R;
import com.ellcie_healthy.ble_library.ble.fall.FallAlertFeature;
import com.ellcie_healthy.ble_library.ble.fall.RescueEvent;
import com.ellcie_healthy.ble_library.ble.models.FallInfo;
import com.ellcie_healthy.ble_library.ble.models.GlassesConfig;
import com.ellcie_healthy.ble_library.ble.models.GlassesInfo;
import com.ellcie_healthy.ble_library.ble.models.SensorsInfo;
import com.ellcie_healthy.ble_library.ble.models.TripInfo;
import com.ellcie_healthy.ble_library.ble.models.streaming.SensorDataConsumer;
import com.ellcie_healthy.ble_library.ble.profile.EHBleManager;
import com.ellcie_healthy.ble_library.ble.profile.battery.BatteryManagerCallbacks;
import com.ellcie_healthy.ble_library.ble.profile.command.callback.CommandEventCallback;
import com.ellcie_healthy.ble_library.ble.profile.command.data.FallState;
import com.ellcie_healthy.ble_library.ble.profile.command.data.LogV2;
import com.ellcie_healthy.ble_library.ble.profile.command.data.SensorType;
import com.ellcie_healthy.ble_library.ble.profile.command.data.SequenceGathering;
import com.ellcie_healthy.ble_library.ble.profile.command.data.SequencePrevention;
import com.ellcie_healthy.ble_library.ble.profile.command.data.ShutdownReason;
import com.ellcie_healthy.ble_library.ble.profile.command.data.SilentModeReason;
import com.ellcie_healthy.ble_library.ble.profile.command.data.TripState;
import com.ellcie_healthy.ble_library.ble.profile.command.data.TripStateInfo;
import com.ellcie_healthy.ble_library.ble.profile.command.data.WarningCode;
import com.ellcie_healthy.ble_library.ble.profile.command.data.cmd.CommandBestMeanValue;
import com.ellcie_healthy.ble_library.ble.profile.command.data.cmd.CommandDebugRisk;
import com.ellcie_healthy.ble_library.ble.profile.command.data.cmd.CommandDisableAlgo;
import com.ellcie_healthy.ble_library.ble.profile.command.data.cmd.CommandFallCommands;
import com.ellcie_healthy.ble_library.ble.profile.command.data.cmd.CommandFallConfig;
import com.ellcie_healthy.ble_library.ble.profile.command.data.cmd.CommandFallGatheringConfirm;
import com.ellcie_healthy.ble_library.ble.profile.command.data.cmd.CommandFallGatheringError;
import com.ellcie_healthy.ble_library.ble.profile.command.data.cmd.CommandFallGatheringGetEventInfo;
import com.ellcie_healthy.ble_library.ble.profile.command.data.cmd.CommandFallGatheringStart;
import com.ellcie_healthy.ble_library.ble.profile.command.data.cmd.CommandFallGatheringStop;
import com.ellcie_healthy.ble_library.ble.profile.command.data.cmd.CommandFallPreventionConfirm;
import com.ellcie_healthy.ble_library.ble.profile.command.data.cmd.CommandFallPreventionError;
import com.ellcie_healthy.ble_library.ble.profile.command.data.cmd.CommandFallPreventionGetEventInfo;
import com.ellcie_healthy.ble_library.ble.profile.command.data.cmd.CommandFallPreventionStart;
import com.ellcie_healthy.ble_library.ble.profile.command.data.cmd.CommandFallPreventionStop;
import com.ellcie_healthy.ble_library.ble.profile.command.data.cmd.CommandGetBatterySoc;
import com.ellcie_healthy.ble_library.ble.profile.command.data.cmd.CommandGetFallStatus;
import com.ellcie_healthy.ble_library.ble.profile.command.data.cmd.CommandGetLogStatus;
import com.ellcie_healthy.ble_library.ble.profile.command.data.cmd.CommandGetLogV2;
import com.ellcie_healthy.ble_library.ble.profile.command.data.cmd.CommandGetSilentModeNotif;
import com.ellcie_healthy.ble_library.ble.profile.command.data.cmd.CommandGetTripStatus;
import com.ellcie_healthy.ble_library.ble.profile.command.data.cmd.CommandIncomingCall;
import com.ellcie_healthy.ble_library.ble.profile.command.data.cmd.CommandLocalizeMe;
import com.ellcie_healthy.ble_library.ble.profile.command.data.cmd.CommandOpticianTest;
import com.ellcie_healthy.ble_library.ble.profile.command.data.cmd.CommandResponseCode;
import com.ellcie_healthy.ble_library.ble.profile.command.data.cmd.CommandResponseCodeSetTrip;
import com.ellcie_healthy.ble_library.ble.profile.command.data.cmd.CommandSetAlarmLuminosity;
import com.ellcie_healthy.ble_library.ble.profile.command.data.cmd.CommandSetAlarmVolume;
import com.ellcie_healthy.ble_library.ble.profile.command.data.cmd.CommandSetAlwaysWarning;
import com.ellcie_healthy.ble_library.ble.profile.command.data.cmd.CommandSetFullConfig;
import com.ellcie_healthy.ble_library.ble.profile.command.data.cmd.CommandSetNotifyLuminosity;
import com.ellcie_healthy.ble_library.ble.profile.command.data.cmd.CommandSetNotifyVolume;
import com.ellcie_healthy.ble_library.ble.profile.command.data.cmd.CommandSetSilentMode;
import com.ellcie_healthy.ble_library.ble.profile.command.data.cmd.CommandSetStreaming;
import com.ellcie_healthy.ble_library.ble.profile.command.data.cmd.CommandSetTimestamp;
import com.ellcie_healthy.ble_library.ble.profile.command.data.cmd.CommandSetTripStatus;
import com.ellcie_healthy.ble_library.ble.profile.command.data.cmd.CommandShutdown;
import com.ellcie_healthy.ble_library.ble.profile.command.data.cmd.CommandStreetAlarm;
import com.ellcie_healthy.ble_library.ble.profile.command.data.cmd.CommandUnmuteStreetAlarm;
import com.ellcie_healthy.ble_library.ble.profile.command.data.event.EventCode;
import com.ellcie_healthy.ble_library.ble.profile.command.data.event.EventData;
import com.ellcie_healthy.ble_library.ble.profile.command.data.event.EventDataFall;
import com.ellcie_healthy.ble_library.ble.profile.command.data.event.EventDataLocalize;
import com.ellcie_healthy.ble_library.ble.profile.command.data.event.EventDataShutdown;
import com.ellcie_healthy.ble_library.ble.profile.command.data.event.EventDataSilentMode;
import com.ellcie_healthy.ble_library.ble.profile.command.data.event.EventDataTap;
import com.ellcie_healthy.ble_library.ble.profile.command.data.event.EventDataTrip;
import com.ellcie_healthy.ble_library.ble.profile.command.data.event.EventDataWarning;
import com.ellcie_healthy.ble_library.ble.profile.fota.FotaCallbacks;
import com.ellcie_healthy.ble_library.ble.profile.fota.data.FotaEvent;
import com.ellcie_healthy.ble_library.ble.profile.fota.data.ImageData;
import com.ellcie_healthy.ble_library.ble.profile.generic.information.DeviceInformationCallbacks;
import com.ellcie_healthy.ble_library.ble.profile.measure.MeasureCallbacks;
import com.ellcie_healthy.ble_library.ble.profile.measure.MeasureService;
import com.ellcie_healthy.ble_library.ble.profile.measure.data.RiskData;
import com.ellcie_healthy.ble_library.ble.utils.Utils;
import com.ellcie_healthy.common.ServiceConstant;
import com.ellcie_healthy.common.callbacks.EllcieCommonCallbackGetBoolean;
import com.ellcie_healthy.common.callbacks.EllcieCommonCallbackGetGeneric;
import com.ellcie_healthy.common.callbacks.EllcieCommonOtaFinishCallback;
import com.ellcie_healthy.common.callbacks.EllcieCommonOtaProgressCallback;
import com.ellcie_healthy.common.utils.com.ellcie_healthy.common.otaUtils.ChecksumUtils;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import kotlin.jvm.JvmField;
import no.nordicsemi.android.ble.data.Data;


public class EHBleForegroundService extends BleForegroundService implements
        DeviceInformationCallbacks, BatteryManagerCallbacks, CommandEventCallback,
        MeasureCallbacks, FotaCallbacks {
    private static final String TAG = EHBleForegroundService.class.getSimpleName();

    private static final String CONNECTED_GLASSES_CHANNEL = "connected_glasses_channel";

    private final static String ACTION_DISCONNECT = TAG + ".ACTION_DISCONNECT";
    private final static int NOTIFICATION_ID = 519;
    private final static int NOTIFICATION_ACTION_OPEN = 0;
    private final static int NOTIFICATION_ACTION_DISCONNECT = 1;
    public static final int KILL_ELLCIE_REQUEST_CODE = 456; //Request code value used by the notification service for killing service and associated app.

    private final Semaphore mSyncEvents = new Semaphore(1);
    private Context context  = null ;

    private final MutableLiveData<Boolean> mTapsEnable = new MutableLiveData<>(true);
    private final MutableLiveData<TripInfo> mTripData = new MutableLiveData<>();
    private final MutableLiveData<FallInfo> mFallData = new MutableLiveData<>();
    private final MutableLiveData<Integer> mBatteryLevel = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> mIsCharging = new MutableLiveData<>(false);
    private final MutableLiveData<GlassesInfo> mGlassesData = new MutableLiveData<>();
    private final MutableLiveData<SensorsInfo> mSensorsData = new MutableLiveData<>();
    private final MutableLiveData<WarningCode> mWarning = new MutableLiveData<>();
    private final MutableLiveData<ShutdownReason> mShutDown = new MutableLiveData<>();

    private SequenceGathering mSequenceGathering;
    private SequencePrevention mSequencePrevention;

    private final MutableLiveData<Boolean> mIsReady = new MutableLiveData<>();

    private String mDisconnectedSerialNumber;

    private final MutableLiveData<SilentModeReason> mIsSilentMode = new MutableLiveData<>();

    private final GlassesInfo mGlasses = new GlassesInfo();
    private final TripInfo mTripInfo = new TripInfo();
    private final FallInfo mFallInfo = new FallInfo();
    private final SensorsInfo mSensorsInfo = new SensorsInfo();
    private GlassesConfig mGlassesConfig = new GlassesConfig(5, 5, 50, 50, false, false, false,-1 );

    private final EHBinder mBinder = new EHBinder();
    private StreamDataConsumer mStreamDataConsumer;
    private CommandsRunnable mCmdRunnable = null;
    final BroadcastReceiver disconnectActionBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            Log.i(TAG, "[Notification] Disconnect action pressed");
            stopService();
        }
    };
    private Boolean mServiceRunning = false;
    private final Object mSync = new Object();

    private FallAlertFeature mFallAlertFeature;

    private EllcieCommonCallbackGetGeneric<Intent> mFallCallback;


//    private KillEllcieBroadcastReceiver mEventsBroadcastReceiveradcast;


    private Class<?> mClazz;


    protected void stopService() {
        Log.d(TAG, "stopService");

        if (isConnected() && (mTripInfo.getState() != TripState.STOPPED)) {
            new Thread(() -> {
                stopTrip(true, false);
                EHBleForegroundService.super.stopService();
            }).start();
        } else {
            super.stopService();
        }
    }

    @Override
    protected EHBinder getBinder() {
        return mBinder;
    }

    @Override
    protected EHBleManager initializeManager() {
        EHBleManager manager = new EHBleManager(this, this, this, this, this);
        //mStreamDataConsumer = new StreamDataConsumer(this, manager.getMeasureService());
        return manager;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mIsCharging.setValue(false);

        final IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_DISCONNECT);
        registerReceiver(disconnectActionBroadcastReceiver, filter);
        context =  this;
    }

    @Override
    protected boolean shouldAutoConnect() {
        return false;
    }

    @Override
    public void onDestroy() {
//        Log.d(TAG, "onDestroy: STOP SERVICE");

        try {
            getBinder().disconnect();
        } catch (Exception ignored) {
        }

        if(mStreamDataConsumer != null) {
            mStreamDataConsumer.closeAll();
        }

        mClazz = null;

        if (mFallAlertFeature != null) {
            mFallAlertFeature.stop();
        }

        cancelNotification(context);

        try {
            context.unregisterReceiver(disconnectActionBroadcastReceiver);

//            if (mEventsBroadcastReceiveradcast != null) {
//                unregisterReceiver(mEventsBroadcastReceiveradcast);
//            }
        } catch (IllegalArgumentException err) {
            err.printStackTrace();
        }

        super.onDestroy();
    }

    public void onKill(Context context){
        Log.d(TAG, "onDestroy: STOP SERVICE");

        try {
            getBinder().disconnect();
        } catch (Exception ignored) {
        }

        if(mStreamDataConsumer != null) {
            mStreamDataConsumer.closeAll();
        }

        mClazz = null;

        if (mFallAlertFeature != null) {
            mFallAlertFeature.stop();
        }

        cancelNotification(context);

        try {
            context.unregisterReceiver(disconnectActionBroadcastReceiver);

//            if (mEventsBroadcastReceiveradcast != null) {
//                unregisterReceiver(mEventsBroadcastReceiveradcast);
//            }
        } catch (IllegalArgumentException err) {
            err.printStackTrace();
        }
    }

    @Override
    protected void onServiceRebind(final Intent intent) {
        synchronized (mSync) {
            stopForegroundService();
            mServiceRunning = false;
        }
    }

    @Override
    protected void onServiceUnbind(final Intent intent) {
        synchronized (mSync) {
            mBinder.delAllSensorStreamingListener(intent);
            startForegroundService(mClazz);
            mStreamDataConsumer.flushAll();
            mServiceRunning = true;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    protected void onDeviceConnecting() {
        super.onDeviceConnecting();
        updateNotification(mClazz);
    }

    @Override
    protected void onDeviceInitializing() {
        super.onDeviceInitializing();
        updateNotification(mClazz);
    }

    @Override
    protected void onDeviceConnected(boolean forceConfig) {

        synchronized (mSync) {
            mServiceRunning = true;
        }
        updateNotification(mClazz);

        sendInitCommands(forceConfig);
    }

    @Override
    protected void onDeviceDisconnected(int reason) {
        super.onDeviceDisconnected(reason);

        synchronized (mSync) {
            mServiceRunning = false;
        }
        updateNotification(mClazz);
        clearDatas();
        if (mFallAlertFeature != null) {
            mFallAlertFeature.setTimeStampDisconnected(System.currentTimeMillis());
        }
    }

    @Override
    protected void onDeviceDisconnecting() {
        super.onDeviceDisconnecting();

        updateNotification(mClazz);
    }

    @Override
    protected boolean stopWhenDisconnected() {
        return false;
    }

    public void setStreamDataConsumer(StreamDataConsumer dataConsumer) {
        mStreamDataConsumer.flushAll();
        mStreamDataConsumer = dataConsumer;
    }

    private void startForegroundService(Class<?> clazz) {
        // when the activity closes we need to show the notification that user is connected to the peripheral sensor
        // We start the service as a foreground service as Android 8.0 (Oreo) onwards kills any running background services
        final Notification notification = createNotification(clazz);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel chan = new NotificationChannel(NOTIFICATION_DEFAULT_PRIORITY_CHANNEL_ID,
                    "Driver service", NotificationManager.IMPORTANCE_NONE);
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(chan);
            startForeground(NOTIFICATION_ID, notification);
        } else {
            final NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            nm.notify(NOTIFICATION_ID, notification);
        }
    }

    /**
     * Stops the service as a foreground service
     */
    private void stopForegroundService() {
        // when the activity rebinds to the service, remove the notification and stop the foreground service
        // on devices running Android 8.0 (Oreo) or above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true);
        } else {
            cancelNotification();
        }
    }

    @SuppressWarnings("SameParameterValue")
    private Notification createNotification(Class clazz) {

        String message = "";
        String deviceName = getName();
        switch (mBinder.getConnectionState().getValue().getState()) {
            case CONNECTING:
                if ((deviceName == null) || deviceName.isEmpty()) {
                    message = getString(R.string.device_status_connecting_no_name);
                } else {
                    message = getString(R.string.device_status_connecting, deviceName);
                }
                break;
            case DISCONNECTED:
                if ((deviceName == null) || deviceName.isEmpty()) {
                    message = getString(R.string.device_status_disconnected_no_name);
                } else {
                    message = getString(R.string.device_status_disconnected, deviceName);
                }
                break;
            case DISCONNECTING:
                if ((deviceName == null) || deviceName.isEmpty()) {
                    message = getString(R.string.device_status_disconnecting_no_name);
                } else {
                    message = getString(R.string.device_status_disconnecting, deviceName);
                }
                break;
            case INITIALIZING:
                message = getString(R.string.device_status_initializing, deviceName);
                break;
            case READY:
                message = getString(R.string.device_status_connected, deviceName);
                break;
        }

        Bitmap iconEllcie = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

        NotificationCompat.Builder builder;
        builder = new NotificationCompat.Builder(this, NOTIFICATION_DEFAULT_PRIORITY_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_logo_ellcie_healthy)
                .setLargeIcon(iconEllcie) //@FIX cannot find icon on oneplus 3
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message)) //exendable text, to show the text in his integrality
                .setContentTitle("Ellcie Service")
                .setContentText(message)
                .setLights(NOTIFICATION_LIGHT, TIME_LIGHT_ON, TIME_LIGHT_OFF) //set light
                .setAutoCancel(true);


        if (clazz != null) {//check here passing to the notification to avoid crash
            /*final Intent targetIntent = new Intent(this, clazz);
            targetIntent.putExtra(BleForegroundService.EXTRA_DEVICE_ADDRESS, getBluetoothDevice().getAddress());*/
            Intent notificationIntent = new Intent(this, clazz);
            notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity((this),
                    (int) System.currentTimeMillis(), notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(pendingIntent);// Set the class (homeactivty) that will be called when the user click on the notification and only if the clazz object is not null
        }


        //In comment for now and will be used when this action will be functionnal from the notification bar
       /* Intent intent = new Intent(KillEllcieBroadcastReceiver.INTENT_FILTER_VALUE);
        intent.putExtra(KillEllcieBroadcastReceiver.CLASS, clazz);
        PendingIntent hide = PendingIntent.getBroadcast(this, KILL_ELLCIE_REQUEST_CODE, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.addAction(0, getString(R.string.close_ellcie_app_text), hide);*/

        return builder.build();
    }

    private void updateNotification(Class clazz) {
        synchronized (mSync) {
            if (mServiceRunning) {
                final NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                nm.notify(NOTIFICATION_ID, createNotification(clazz));
            }
        }
    }

    private void cancelNotification() {
        final NotificationManager nm = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        nm.cancel(NOTIFICATION_ID);
    }

    private void cancelNotification(Context context) {
        final NotificationManager nm = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        nm.cancel(NOTIFICATION_ID);
    }


    private void setRootStreamingUri(Uri uri) {
//        Log.d(TAG, "setRootStreamingUri: " + uri);
        mStreamDataConsumer.setRoot(uri);
    }

    private boolean checkStreamingSensors(@Nullable ArrayList<SensorType> streamingSensors) {
        if (streamingSensors == null || streamingSensors.size() <= 0) return false;

        for (SensorType s : streamingSensors) {
            switch (s) {
                case EYE_SENSOR_RIGHT_DOWN:
                case EYE_SENSOR_LEFT_DOWN:
                case ATMO_PRESSURE:
                case GYROSCOPE:
                case ACC_GYRO:
                case ACCELEROMETER:
                case LOOK_DIRECTION:
                case HEAD_ROTATION:
                    break;
                default:
                    Log.e(TAG, "not supported sensor: " + s);
                    return false;
            }
        }

        return true;
    }

    private boolean shutdown() {
        mCmdRunnable.addCommand(new GlassCommand(new CommandShutdown(), COMMAND_WRITE_TIMEOUT, response -> {
            if (response.isValid()) {
                Log.d(TAG, "shutdown initialized");
            } else {
                Log.e(TAG, "shutdown not performed");
            }
        }));

        return true;
    }

    private boolean startTrip(@Nullable ArrayList<SensorType> streamingSensors, boolean disableAlgo) {
        if (mTripInfo.getState() != TripState.STOPPED) {
            Log.d(TAG, "trip already started");
            return false;
        }

        if (!mIsReady.getValue()) {
            Log.e(TAG, "Connection not ready");
            return false;
        }
        if (disableAlgo) {

            mCmdRunnable.addCommand(new GlassCommand(new CommandDisableAlgo(disableAlgo), COMMAND_WRITE_TIMEOUT, response -> {
                CommandDisableAlgo lDisableAlgo = (CommandDisableAlgo) response;
                if (!lDisableAlgo.isValid()) {
                    Log.w(TAG, "unable to disable algo!");
                } else {
                    Log.d(TAG, "Algo disabled");
                }
            }));
        }

        if (checkStreamingSensors(streamingSensors)) {
            Log.d(TAG, "setup streaming: " + streamingSensors);

            mCmdRunnable.addCommand(new GlassCommand(new CommandSetStreaming(streamingSensors), COMMAND_WRITE_TIMEOUT, () -> {
//                mBleManager.requestConnectionPriorityHigh();

            }, response -> {
                CommandSetStreaming streaming = (CommandSetStreaming) response;
                if (!streaming.isValid() || streaming.getResponseCode() != CommandResponseCode.OK) {
                    Log.e(TAG, "unable to setup streaming");
//                    mBleManager.requestConnectionPriorityBalanced();
                    setTripState(TripState.STOPPED, TripStateInfo.NONE, -1, -1, 0, "");
                    return;
                }

               // mBleManager.getMeasureService().startListeners();


                mCmdRunnable.addCommand(new GlassCommand(new CommandSetTripStatus(true), COMMAND_WRITE_TIMEOUT, response1 -> {
                    CommandSetTripStatus setTripStatus = (CommandSetTripStatus) response1;
                    if (!setTripStatus.isValid() || setTripStatus.getStatus() != CommandResponseCodeSetTrip.OK) {
                        Log.e(TAG, "unable to start trip");
                       // mBleManager.getMeasureService().stopListeners();
//                        mBleManager.requestConnectionPriorityBalanced();

                        TripStateInfo reason = TripStateInfo.NONE;

                        if (setTripStatus.getStatus() == CommandResponseCodeSetTrip.IN_CHARGE) {
                            reason = TripStateInfo.STOP_CAUSE_CHARGER;
                        }

                        setTripState(TripState.STOPPED, reason, -1, -1, 0, "");
                    } else {
                        setTripState(TripState.STARTED_CALIBRATION, TripStateInfo.NONE, System.currentTimeMillis(), -1, setTripStatus.getTripId(), setTripStatus.getDriverTripId());
                    }
                }));
            }));
        } else {
            mCmdRunnable.addCommand(new GlassCommand(new CommandSetTripStatus(true), COMMAND_WRITE_TIMEOUT, response1 -> {
                CommandSetTripStatus setTripStatus = (CommandSetTripStatus) response1;
                if (!setTripStatus.isValid() || setTripStatus.getStatus() != CommandResponseCodeSetTrip.OK) {
                    Log.e(TAG, "unable to start trip");
                    //mBleManager.getMeasureService().stopListeners();

                    TripStateInfo reason = TripStateInfo.NONE;

                    if (setTripStatus.getStatus() == CommandResponseCodeSetTrip.IN_CHARGE) {
                        reason = TripStateInfo.STOP_CAUSE_CHARGER;
                    }

                    setTripState(TripState.STOPPED, reason, -1, -1, 0, "");
                } else {
                    setTripState(TripState.STARTED_CALIBRATION, TripStateInfo.NONE, System.currentTimeMillis(), -1, setTripStatus.getTripId(), setTripStatus.getDriverTripId());
                }
            }));
        }

        return true;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        // do nothing
    }

    private boolean stopTrip(final boolean wait, final boolean updateAlgoState) {
        if (mTripInfo.getState() == TripState.STOPPED) {
            Log.i(TAG, "trip already stopped");
            return false;
        }

        if (!mIsReady.getValue()) {
            Log.e(TAG, "Connection not ready");
            return false;
        }

        final Semaphore sem = new Semaphore(0);

        mCmdRunnable.addCommand(new GlassCommand(new CommandSetTripStatus(false), 2000, () -> {
//            mBleManager.requestConnectionPriorityHigh();
            //mBleManager.getMeasureService().stopListeners();
        }, response -> {
            CommandSetTripStatus setTripStatus = (CommandSetTripStatus) response;
            if (!setTripStatus.isValid() ||
                    (setTripStatus.getStatus() != CommandResponseCodeSetTrip.OK &&
                            setTripStatus.getStatus() != CommandResponseCodeSetTrip.ALREADY_STOPPED)) {
                Log.e(TAG, "unable to stop trip");
            } else {
                Log.i(TAG, "trip stopped");
            }

            setTripState(TripState.STOPPED, TripStateInfo.NONE, -1, -1, 0, "");
        }));

        if (updateAlgoState) {
            mCmdRunnable.addCommand(new GlassCommand(new CommandDisableAlgo(false), COMMAND_WRITE_TIMEOUT, response -> {
                CommandDisableAlgo disableAlgo = (CommandDisableAlgo) response;
                if (!disableAlgo.isValid()) {
                    Log.w(TAG, "unable to enable algo!");
                } else {
                    Log.i(TAG, "Algo enabled");
                }

                sem.release();
            }));
        }

        if (wait) {
            boolean acquired = false;
            try {
                acquired = sem.tryAcquire(4000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ignored) {
            }

            if (!acquired) {
                Log.e(TAG, "unable to acquire sem");
            }
        }

        return true;
    }

    private void setGlassesConfig(@NonNull GlassesConfig config) {
        mGlassesConfig = config;
    }

    private boolean toggleTap() {
        final boolean newValue = !mTapsEnable.getValue();
        mGlassesConfig.setSilentMode(!newValue);
        mCmdRunnable.addCommand(new GlassCommand(new CommandSetSilentMode(!newValue), COMMAND_WRITE_TIMEOUT, response -> {
            if (response.isValid()) {
                Log.d(TAG, "tap " + (newValue ? "enable" : "disabled"));
                mTapsEnable.postValue(newValue);
            } else {
                Log.e(TAG, "unable to set taps");
            }
        }));

        return true;
    }

    private boolean localizeMe() {
        mCmdRunnable.addCommand(new GlassCommand(new CommandLocalizeMe(), 2000, response -> {
            if (!response.isValid()) {
                Log.e(TAG, "unable to send localize");
            }
        }));

        return true;
    }

    private boolean sosFall() {
        mCmdRunnable.addCommand(new GlassCommand(new CommandFallCommands(CommandFallCommands.FallCommandCode.ENGAGE_SOS), 2000, response -> {
            if (!response.isValid()) {
                Log.e(TAG, "unable to send engage SOS");
            }
        }));
        return true;

    }

    private boolean cancelSosFallAlert() {
        mCmdRunnable.addCommand(new GlassCommand(new CommandFallCommands(CommandFallCommands.FallCommandCode.CANCEL_SOS), 2000, response -> {
            if (!response.isValid()) {
                Log.e(TAG, "Unable to send cancel SOS");
            }
        }));
        return true;

    }

    private boolean cancelFallAlert() {
        mCmdRunnable.addCommand(new GlassCommand(new CommandFallCommands(CommandFallCommands.FallCommandCode.CANCEL_FALL), 2000, response -> {
            if (!response.isValid()) {
                Log.e(TAG, "Unable to send cancel Fall");
            }
        }));
        return true;

    }

    private boolean confirmFallAlert() {
        mCmdRunnable.addCommand(new GlassCommand(new CommandFallCommands(CommandFallCommands.FallCommandCode.CONFIRM_FALL), 2000, response -> {
            if (!response.isValid()) {
                Log.e(TAG, "Unable to send confirm SOS");
            }
        }));
        return true;
    }

    private boolean enableFall() {
        mCmdRunnable.addCommand(new GlassCommand(new CommandFallCommands(CommandFallCommands.FallCommandCode.ENABLE_FALL), 2000, response -> {
            if (!response.isValid()) {
                Log.e(TAG, "Unable to send enable fall");
            }
        }));
        return true;
    }

    private boolean disableFall() {
        mCmdRunnable.addCommand(new GlassCommand(new CommandFallCommands(CommandFallCommands.FallCommandCode.DISABLE_FALL), 2000, response -> {
            if (!response.isValid()) {
                Log.e(TAG, "Unable to send disable fall");
            }
        }));
        return true;
    }

    private boolean confirmSosAlert() {
        mCmdRunnable.addCommand(new GlassCommand(new CommandFallCommands(CommandFallCommands.FallCommandCode.CONFIRM_SOS), 2000, response -> {
            if (!response.isValid()) {
                Log.e(TAG, "Unable to send confirm SOS");
            }
        }));
        return true;
    }

    private boolean helperAcquittal() {
        mCmdRunnable.addCommand(new GlassCommand(new CommandFallCommands(CommandFallCommands.FallCommandCode.ACK), 2000, response -> {
            if (!response.isValid()) {
                Log.e(TAG, "Unable to send confirm SOS");
            }
        }));
        return true;
    }

    private boolean setAlarmVolum(int buzzerVolum) {
        mGlassesConfig.setAlarmVolume(buzzerVolum);

        mCmdRunnable.addCommand(new GlassCommand(new CommandSetAlarmVolume(buzzerVolum), 2000, response -> {
            if (!response.isValid()) {
                Log.e(TAG, "Unable to set a new alarm volume");
            }
        }));
        return true;
    }

    private boolean setAlarmLuminosity(int lightIntensity) {
        mGlassesConfig.setAlarmLuminosity(lightIntensity);

        mCmdRunnable.addCommand(new GlassCommand(new CommandSetAlarmLuminosity(lightIntensity), 2000, response -> {
            if (!response.isValid()) {
                Log.e(TAG, "Unable to set a new alarm light intensity");
            }
        }));
        return true;
    }

    private boolean setNotifLuminosity(int lightIntensity) {
        mGlassesConfig.setNotifLuminosity(lightIntensity);
        mCmdRunnable.addCommand(new GlassCommand(new CommandSetNotifyLuminosity(lightIntensity), 2000, response -> {
            if (!response.isValid()) {
                Log.e(TAG, "Unable to set a new notif light intensity");
            }
        }));
        return true;
    }

    private boolean setAlwaysAlert(boolean alwaysAlert) {
        if (mCmdRunnable != null) {
            mCmdRunnable.addCommand(new GlassCommand(new CommandSetAlwaysWarning(alwaysAlert), 2000, response -> {
                if (!response.isValid()) {
                    Log.e(TAG, "Unable to send always alert state");
                }
            }));
        }
        return true;
    }

    private boolean setNotifVolum(int notifVolum) {
        mGlassesConfig.setNotifVolume(notifVolum);
        mCmdRunnable.addCommand(new GlassCommand(new CommandSetNotifyVolume(notifVolum), 2000, response -> {
            if (!response.isValid()) {
                Log.e(TAG, "Unable to send always alert state");
            }
        }));
        return true;
    }

    private void setAlgoSensitivity(int sensitivity) {
        mGlassesConfig.setSensitivityLevel(sensitivity);

        Log.d(TAG, "setAlgoSensitivity");
        if (!mIsReady.getValue()) {
            Log.e(TAG, "Connection not ready");
            return;
        }

        String firmware = mGlasses.getFirmware();
        if (firmware == null) {
            firmware = Utils.DEFAULT_FIRMWARE_VERSION;
        }
        if (Utils.compareVersion(firmware, Utils.MIN_VERSION_SET_ALGO_SENSITIVITY) < 0) {
            Log.d(TAG, "firmware lower than supported version");
            return;
        }
        if(!mGlassesConfig.isValidSensitivity()){
            Log.d(TAG, "sensitivity not valid");
            return;
        }
        mCmdRunnable.addCommand(new GlassCommand(new CommandSetFullConfig(mGlassesConfig.getAlarmVolume(), mGlassesConfig.getNotifVolume(),
                mGlassesConfig.getAlarmLuminosity(), mGlassesConfig.getNotifLuminosity(),
                mGlassesConfig.isSilentMode(),
                TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()),
                mGlassesConfig.isEnableSos(), mGlassesConfig.isEnableFaiting(),
                mGlassesConfig.getSensitivityLevel()), COMMAND_WRITE_TIMEOUT, response -> {
            if (!response.isValid()) {
                Log.e(TAG, "Unable to set fullconfig with sensitivity");
            }
        }));
    }

    private boolean setComandIncomingCall() {
        mCmdRunnable.addCommand(new GlassCommand(new CommandIncomingCall(), 2000, response -> {
            if (!response.isValid()) {
                Log.e(TAG, "Unable to send incoming call command");
            }
        }));
        return true;
    }

    private void getBestMeanValue(GlassCommand.GlassCommandCallback callback) {
        mCmdRunnable.addCommand(new GlassCommand(new CommandBestMeanValue(), 2000, response -> {
            if (!response.isValid()) {
                Log.e(TAG, "Unable to retrieve best mean value");
            }
            callback.onCommandResponse(response);
        }));
    }

    private boolean playStreetAlarm() {
        mCmdRunnable.addCommand(new GlassCommand(new CommandStreetAlarm(), 2000, response -> {
            if (!response.isValid()) {
                Log.e(TAG, "Unable to play street alarm");
            }
        }));
        return true;
    }

    private boolean streetlabUnmuteAlarm() {
        mCmdRunnable.addCommand(new GlassCommand(new CommandUnmuteStreetAlarm(), 2000, response -> {
            if (!response.isValid()) {
                Log.e(TAG, "Unable to play street alarm");
            }
        }));
        return true;
    }

    private boolean sendDebugRisk(byte level) {
        mCmdRunnable.addCommand(new GlassCommand(new CommandDebugRisk(level), 2000, response -> {
            if (!response.isValid()) {
                Log.e(TAG, "Unable to send debug risk level");
            }
        }));
        return true;

    }

    private boolean setFullConfig(int buzzerVolume, int buzzerVolumeNotification, int lightIntensity, int lightNotification, boolean sosGlasses, int sensitivity) {
        mGlassesConfig.setAlarmVolume(buzzerVolume);
        mGlassesConfig.setNotifVolume(buzzerVolumeNotification);
        mGlassesConfig.setAlarmLuminosity(lightIntensity);
        mGlassesConfig.setNotifLuminosity(lightNotification);
        mGlassesConfig.setEnableSos(sosGlasses);
        mGlassesConfig.setSensitivityLevel(sensitivity);

        String firmware = mGlasses.getFirmware();
        if (firmware == null) {
            firmware = Utils.DEFAULT_FIRMWARE_VERSION;
        }
        if (Utils.compareVersion(firmware, Utils.MIN_VERSION_SET_ALGO_SENSITIVITY) < 0 || !mGlassesConfig.isValidSensitivity()) {
            mCmdRunnable.addCommand(new GlassCommand(new CommandSetFullConfig(buzzerVolume, buzzerVolumeNotification,
                    lightIntensity, lightNotification,
                    mGlassesConfig.isSilentMode(),
                    TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()),
                    sosGlasses, mGlassesConfig.isEnableFaiting()), COMMAND_WRITE_TIMEOUT));
        } else {
            mCmdRunnable.addCommand(new GlassCommand(new CommandSetFullConfig(buzzerVolume, buzzerVolumeNotification,
                    lightIntensity, lightNotification,
                    mGlassesConfig.isSilentMode(),
                    TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()),
                    sosGlasses, mGlassesConfig.isEnableFaiting(),
                    sensitivity), COMMAND_WRITE_TIMEOUT));
        }

        return true;
    }

    private boolean startOpticianTest() {
        mCmdRunnable.addCommand(new GlassCommand(new CommandOpticianTest(0x1), 2000, response -> {
            if (!response.isValid()) {
                Log.e(TAG, "Unable to start Optician test");
            }
        }));

        return true;
    }

    private void stopOpticianTest(GlassCommand.GlassCommandCallback callback) {
        mCmdRunnable.addCommand(new GlassCommand(new CommandOpticianTest((byte) 0x0), 2000, response -> {
            if (!response.isValid()) {
                Log.e(TAG, "Unable to stop Optician test");
            } else {
                CommandOpticianTest opticianTest = (CommandOpticianTest) response;

                Log.d(TAG, "Got " + opticianTest.getNbBlink());
            }
            callback.onCommandResponse(response);
        }));
    }


    private boolean muteGlasses() {
        mCmdRunnable.addCommand(new GlassCommand(new CommandSetSilentMode(true), 2000, response -> {
            if (!response.isValid()) {
                Log.e(TAG, "Unable to mute glasses");
            }
        }));
        return true;
    }

    private boolean unmuteGlasses() {
        mCmdRunnable.addCommand(new GlassCommand(new CommandSetSilentMode(false), 2000, response -> {
            if (!response.isValid()) {
                Log.e(TAG, "Unable to mute glasses");
            }
        }));
        return true;
    }


    private void finalizeInit() {
        mBleManager.requestConnectionPriorityBalanced();
        //Push directly on the main thread
        mIsReady.setValue(true);

    }

    private void getDriverGlassesLog(EllcieCommonCallbackGetGeneric<ArrayList<LogV2>> cb, @NonNull ArrayList<LogV2> logList) {

        if (mCmdRunnable == null) {
            //can happen in certain case, need some more time to investigate how this value can be null
            Log.w(TAG, "mCmdRunnable is null");
            cb.done(logList);
            return;
        }

        mCmdRunnable.addCommand(new GlassCommand(new CommandGetLogV2(), COMMAND_WRITE_TIMEOUT, response -> {
            CommandGetLogV2 logV2 = (CommandGetLogV2) response;
            if (!logV2.isValid()) {
                Log.e(TAG, "unable to get logs");
                cb.done(logList);
                return;
            }

            logList.addAll(logV2.getLogs());
            if (logV2.getStatus() == CommandGetLogStatus.LOG_STATUS_OK) {
                getDriverGlassesLog(cb, logList);
                return;
            }

            Log.d(TAG, "no more logs to read");
            if (cb != null) {
                cb.done(logList);
            }
        }));
    }


    private void sendInitCommands(final boolean forceConfig) {
        Executor executor = Executors.newSingleThreadExecutor();
        if (mCmdRunnable != null) {
            mCmdRunnable.clear();
            mCmdRunnable.stop();
        }

        mCmdRunnable = new CommandsRunnable(mBleManager);
        executor.execute(mCmdRunnable);

        mCmdRunnable.addCommand(new GlassCommand(new CommandGetSilentModeNotif(),
                COMMAND_WRITE_TIMEOUT, () -> {
            mBleManager.requestConnectionPriorityHigh();
            Log.d(TAG, "drainPermits");
            mSyncEvents.drainPermits();
        }, response -> {
            CommandGetSilentModeNotif silentModeNotif = (CommandGetSilentModeNotif) response;
            Log.d(TAG, "silentModeNotif: " + silentModeNotif.isValid() + " - " + silentModeNotif.getResponseCode() + " - " + silentModeNotif.getRawData());

            boolean tapEnabled = mTapsEnable.getValue();
            try {
                if (mSyncEvents.tryAcquire(2, TimeUnit.SECONDS)) {
                    tapEnabled = mTapsEnable.getValue();
                } else {
                    Log.w(TAG, "unable to receive tap notify");
                }
                Log.d(TAG, "syncEvents tapEnabled: " + tapEnabled);
            } catch (InterruptedException ignored) {
            }
        }));

        if (forceConfig) {
            Log.d(TAG, "Will force config");
            String firmware = mGlasses.getFirmware();
            if (firmware == null) {
                firmware = Utils.DEFAULT_FIRMWARE_VERSION;
            }
            if (Utils.compareVersion(firmware, Utils.MIN_VERSION_SET_ALGO_SENSITIVITY) < 0 || !mGlassesConfig.isValidSensitivity()) {
                mCmdRunnable.addCommand(new GlassCommand(new CommandSetFullConfig(mGlassesConfig.getAlarmVolume(), mGlassesConfig.getNotifVolume(),
                        mGlassesConfig.getAlarmLuminosity(), mGlassesConfig.getNotifLuminosity(),
                        mGlassesConfig.isSilentMode(),
                        TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()),
                        mGlassesConfig.isEnableSos(), mGlassesConfig.isEnableFaiting()), COMMAND_WRITE_TIMEOUT));
            } else {
                mCmdRunnable.addCommand(new GlassCommand(new CommandSetFullConfig(mGlassesConfig.getAlarmVolume(), mGlassesConfig.getNotifVolume(),
                        mGlassesConfig.getAlarmLuminosity(), mGlassesConfig.getNotifLuminosity(),
                        mGlassesConfig.isSilentMode(),
                        TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()),
                        mGlassesConfig.isEnableSos(), mGlassesConfig.isEnableFaiting(),
                        mGlassesConfig.getSensitivityLevel()), COMMAND_WRITE_TIMEOUT));
            }
        }


        mCmdRunnable.addCommand(new GlassCommand(new CommandSetTimestamp(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())),
                COMMAND_WRITE_TIMEOUT));


        mCmdRunnable.addCommand(new GlassCommand(new CommandGetTripStatus(), COMMAND_WRITE_TIMEOUT, response -> {
            CommandGetTripStatus tripStatus = (CommandGetTripStatus) response;
            Log.d(TAG, "tripStatus: " + tripStatus.getStatus() + " - " + tripStatus.getDuration() + " - " + tripStatus.getTripId());

            setTripState(tripStatus.getTripStatus(), mTripInfo.getInfo(), System.currentTimeMillis() - tripStatus.getDurationMs(), tripStatus.getDurationMs(), tripStatus.getTripId(), mTripInfo.getDriverTripId());
        }));

        finalizeInit();
    }

    private void setTripState(@NonNull TripState state, @NonNull TripStateInfo info, long startMs, long duration, int id, @NonNull String driverTripId) {
        if (id != mTripInfo.getId()) {
            mTripInfo.clear();
            mTripInfo.setId(id);
            mTripInfo.setDriverTripId(driverTripId);
        }


        mTripInfo.setState(state);
        mTripInfo.setInfo(info);

        if ((id > 0) && (state != TripState.STOPPED)) {
            mTripInfo.setStartTsMs(startMs);
            mTripInfo.setDuration(duration);
        } else {
            mTripInfo.setStartTsMs(-1);
            mTripInfo.setDuration(-1);
        }

        mStreamDataConsumer.setSerialAndTripId(mGlasses.getSerial(), mTripInfo.getId());
        mTripData.postValue(mTripInfo);
    }

    private void startOta(String fileDir, EllcieCommonCallbackGetBoolean cbOtaStarted, EllcieCommonOtaFinishCallback cbOtaCompleted, EllcieCommonOtaProgressCallback cbOtaProgress) {

        mBleManager.startOta(fileDir, cbOtaStarted, cbOtaCompleted, cbOtaProgress);
    }

    private void continueOta(EllcieCommonOtaFinishCallback cbOtaCompleted, EllcieCommonOtaProgressCallback cbOtaProgress) {

        mBleManager.continueOta(cbOtaCompleted, cbOtaProgress);
    }


    private void getGathering(EllcieCommonCallbackGetGeneric<SequenceGathering> cbFinished) {
        mCmdRunnable.addCommand(new GlassCommand(new CommandFallGatheringGetEventInfo(), COMMAND_WRITE_TIMEOUT, response -> {
            CommandFallGatheringGetEventInfo eventInfo = (CommandFallGatheringGetEventInfo) response;
            if (eventInfo == null || !eventInfo.isValid()) {
                cbFinished.done(null);
                return;
            }
            short gatheringSize = eventInfo.getDataSize();
            int nbFrame = eventInfo.getNbFrames();
            int eventId = eventInfo.getId();

            mSequencePrevention = null;
            mSequenceGathering = new SequenceGathering();
            mSequenceGathering.initSequenceGathering(gatheringSize, nbFrame, eventId);
            mSequenceGathering.setEllcieCallback(cbFinished);

            mCmdRunnable.addCommand(new GlassCommand(new CommandFallGatheringStart(), COMMAND_WRITE_TIMEOUT, response1 -> {
                if (!response1.isValid()) {
                    cbFinished.done(null);
                }
            }));
        }));
    }

    private void getPrevention(EllcieCommonCallbackGetGeneric<SequencePrevention> cbFinished) {
        mCmdRunnable.addCommand(new GlassCommand(new CommandFallPreventionGetEventInfo(), COMMAND_WRITE_TIMEOUT, response -> {
            CommandFallPreventionGetEventInfo eventInfo = (CommandFallPreventionGetEventInfo) response;
            if (eventInfo == null || !eventInfo.isValid()) {
                cbFinished.done(null);
                return;
            }
            short preventionSize = eventInfo.getDataSize();
            int nbFrame = eventInfo.getNbFrames();

            mSequenceGathering = null;
            mSequencePrevention = new SequencePrevention();
            mSequencePrevention.initSequencePrevention(preventionSize, nbFrame);
            mSequencePrevention.setEllcieCallback(cbFinished);

            mCmdRunnable.addCommand(new GlassCommand(new CommandFallPreventionStart(), COMMAND_WRITE_TIMEOUT, response1 -> {
                if (!response1.isValid()) {
                    cbFinished.done(null);
                }
            }));
        }));
    }


    private void setFallConfig(boolean sosEnabled) {
        mGlassesConfig.setEnableSos(sosEnabled);
        mGlassesConfig.setEnableFaiting(false);

        mCmdRunnable.addCommand(new GlassCommand(new CommandFallConfig(sosEnabled, false), COMMAND_WRITE_TIMEOUT, response -> {
            if (!response.isValid()) {
                Log.e(TAG, "setFallConfig error");
            }
        }));
    }

    private void getFallStatus() {
        mCmdRunnable.addCommand(new GlassCommand(new CommandGetFallStatus(), COMMAND_WRITE_TIMEOUT, response -> {
            if (!response.isValid()) {
                Log.e(TAG, "getFallStatus error");
            } else {
                mFallInfo.setState(FallState.valueOf(((CommandGetFallStatus) response).getFallStatus().getCode()));
                long ts = System.currentTimeMillis() - (((CommandGetFallStatus) response).getTimestamp() / 1000);
                mFallInfo.setTimestamp(ts);
                mFallData.postValue(mFallInfo);
            }
        }));
    }

    private void getBatteryLevel() {
        mCmdRunnable.addCommand(new GlassCommand(new CommandGetBatterySoc(), COMMAND_WRITE_TIMEOUT, response -> {
            if (!response.isValid()) {
                Log.e(TAG, "getBatteryLevel error");
            } else {
                mBatteryLevel.postValue(((CommandGetBatterySoc) response).getBattery());
            }
        }));
    }

    @Nullable
    private String getDisconnectedSerialNumber() {
        return mDisconnectedSerialNumber;
    }


    @Override
    public void onBatteryPowerState(@NonNull final BluetoothDevice device, Boolean plug, Boolean charging) {
        Log.d(TAG, "onBatteryPowerState: " + plug + " - " + charging);
        mIsCharging.postValue(plug);


    }

    @Override
    public void onBatteryLevel(@NonNull final BluetoothDevice device, int batteryLevel) {
        Log.d(TAG, "onBatteryLevel: " + batteryLevel + "%");
        mBatteryLevel.postValue(batteryLevel);


    }

    @Override
    public void onUnexpectedError(@NonNull BluetoothDevice device, int status, @NonNull String message) {
        Log.w(TAG, "onUnexpectedError: " + message + " - " + status);
    }

    @Override
    public void onFirmwareRevision(@NonNull BluetoothDevice device, @NonNull String firmware) {
        mGlasses.setFirmware(firmware);
        mGlassesData.postValue(mGlasses);
    }

    @Override
    public void onModelNumber(@NonNull BluetoothDevice device, @NonNull String model) {
        mGlasses.setModel(model);
        mGlassesData.postValue(mGlasses);
    }

    @Override
    public void onSerialNumber(@NonNull BluetoothDevice device, @NonNull String serial) {
        mGlasses.setSerial(serial);
        mGlassesData.postValue(mGlasses);
    }

    @Override
    public void onEventLocalizeMyPhone(@NonNull EventDataLocalize event) {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build();
        SoundPool soundPool = new SoundPool.Builder().setMaxStreams(2).setAudioAttributes(audioAttributes).build();

        //Load the sound
        int soundId = soundPool.load(this, R.raw.find_my_phone, 1);
        soundPool.setOnLoadCompleteListener((soundPool1, sampleId, status) -> soundPool.play(soundId, 1, 1, 1, 0, 1));
    }

    @Override
    public void onEventTripStateChange(@NonNull EventDataTrip event) {

        long durationMs = mTripInfo.getDuration();
        long startMs = mTripInfo.getStartTsMs();
        if (startMs <= 0 && (event.getState() == TripState.STARTED_CALIBRATION)) {
            startMs = System.currentTimeMillis();
            durationMs = -1;
        } else if (event.getState() == TripState.STOPPED) {
            startMs = -1;
        }

        setTripState(event.getState(), event.getStateInfo(), startMs, durationMs, event.getTripId(), event.getDriverTripId());
    }

    @Override
    public void onEventWarning(@NonNull EventDataWarning event) {
        mWarning.postValue(event.getCode());
    }

    @Override
    public void onEventTapsMode(@NonNull EventDataTap event) {
        Log.d(TAG, "onEventTapsMode: " + event);
        mGlassesConfig.setSilentMode(!event.areTapEnable());
        mTapsEnable.postValue(event.areTapEnable());
        mSyncEvents.release();
    }

    @Override
    public void onEventFallStateChange(@NonNull EventDataFall event) {
        mFallInfo.clear();
        mFallInfo.setState(event.getState());
        mFallInfo.setTimestamp(event.getTimestamp());
        if (mFallAlertFeature != null) {
            mFallAlertFeature.onFallStatusChanged((byte) mFallInfo.getState().getCode(), System.currentTimeMillis(), true);
        }
        mFallData.postValue(mFallInfo);
    }

    @Override
    public void onEventShutdown(@NonNull EventDataShutdown event) {
        Log.d(TAG, "will shutdown");
        mShutDown.postValue(event.getCode());
    }

    @Override
    public void onSilentMode(@NonNull @NotNull EventDataSilentMode event) {
        Log.d(TAG, "received silent mode event");
        mIsSilentMode.postValue(event.getCode());
    }

    @Override
    public void onUnimplementedEvent(@NonNull EventCode code, @NonNull EventData event) {
        // do nothing
        Log.w(TAG, "unimplemented event: " + event.getData().getByte(0));
    }

    @Override
    public void onDebugData(@NonNull BluetoothDevice device, @NonNull Data data) {

    }

    @Override
    public void onHumidityValue(@NonNull BluetoothDevice device, int humidity) {
        mSensorsInfo.setHumidity(humidity);
        mSensorsData.postValue(mSensorsInfo);

    }

    @Override
    public void onWornValue(@NonNull BluetoothDevice device, int duration) {
        mSensorsInfo.setWornDuration(duration);
        mSensorsData.postValue(mSensorsInfo);
    }

    @Override
    public void onStepValue(@NonNull BluetoothDevice device, int steps) {
        mSensorsInfo.setSteps(steps);
        mSensorsData.postValue(mSensorsInfo);
    }

    @Override
    public void onPressureValue(@NonNull BluetoothDevice device, int pressure) {
        mSensorsInfo.setPressure(pressure);
        mSensorsData.postValue(mSensorsInfo);
    }

    @Override
    public void onRiskValue(@NonNull BluetoothDevice device, @NonNull RiskData risk) {
        if (mTripInfo.getId() == risk.getTripId()) {
            mTripInfo.setRiskLevel(risk.getLevel());
            mTripData.postValue(mTripInfo);
        } else if (mTripInfo.getId() > 0 && risk.getTripId() > 0) {
            Log.e(TAG, "invalid trip id: " + risk.getTripId() + " != " + mTripInfo.getId());
        }
    }


    @Override
    public void onTemperatureValue(@NonNull BluetoothDevice device, int temperature) {
        mSensorsInfo.setTemperature(temperature);
        mSensorsData.postValue(mSensorsInfo);

    }

    @Override
    public void onDataGatheringData(@NonNull BluetoothDevice device, @NonNull Data data) {
        //This method is used for Prevention OR Data Gathering so we need to check for which value this function is called

        byte[] dataValue = data.getValue();
        if (dataValue == null) {

            if (mSequenceGathering != null) {

                Log.e(TAG, "data bytes from gathering Data is null");
                mCmdRunnable.addCommand(new GlassCommand(new CommandFallGatheringStop(), COMMAND_WRITE_TIMEOUT, response -> {
                    if (response.isValid()) {
                        Log.d(TAG, "Stop Gathering that had error finished");

                        getGathering(mSequenceGathering.getEllcieCallback());
                    } else {
                        Log.e(TAG, "cannot stop Gathering that encounters some errors");
                    }
                }));
            } else {
                Log.e(TAG, "data bytes from prevention is null");
                mCmdRunnable.addCommand(new GlassCommand(new CommandFallPreventionStop(), COMMAND_WRITE_TIMEOUT, response -> {
                    if (response.isValid()) {
                        Log.d(TAG, "Stop prevention that had error finished");

                        getPrevention(mSequencePrevention.getEllcieCallback());
                    } else {
                        Log.e(TAG, "cannot stop Prevention that encounters some errors");
                    }
                }));
            }
            return;
        }

        // Debug code used to check checksum value in comment for now
//        Log.d(TAG, "CRC: Wanted: " + String.format("0x%02X", crc) + " - Got: " + String.format("0x%02X", computeCheckSum));
//        Log.d(TAG, "Sequence: Wanted: " + sequenceCounter + " - Got: " + sequenceId);

        if (mSequenceGathering != null) {
            manageDataGathering(dataValue);
        } else {
            managePrevention(dataValue);
        }
    }

    private void manageDataGathering(byte[] dataValue) {
        byte computeCheckSum = ChecksumUtils.computeChecksum(Arrays.copyOfRange(dataValue, 1, 21));
        final ByteBuffer bb = ByteBuffer.allocate(dataValue.length)
                .order(ByteOrder.BIG_ENDIAN)
                .put(dataValue);

        short sequenceId = bb.getShort(18);
        short crc = bb.get(0);
        byte[] dataByte = Arrays.copyOfRange(dataValue, 1, 18);

        if (computeCheckSum != crc) {
            Log.w(TAG, "invalid crc");
            mSequenceGathering.incrementErrorCounter();
            if ((mSequenceGathering.getErrorCounter() % 20) == 1) {
                // restart sequence

                mCmdRunnable.addCommand(new GlassCommand(new CommandFallGatheringError(mSequenceGathering.getSequenceCounter()), COMMAND_WRITE_TIMEOUT, response -> {
                    if (response.isValid()) {
                        Log.d(TAG, "restartSequence Gathering ok");
                    } else {
                        Log.e(TAG, "error in restartSequence Gathering");
                    }
                }));

            } else if (mSequenceGathering.getErrorCounter() >= 40) {
                Log.e(TAG, "Too many errors, stop");

                mCmdRunnable.addCommand(new GlassCommand(new CommandFallGatheringStop(), COMMAND_WRITE_TIMEOUT, response -> {
                    if (response.isValid()) {
                        Log.d(TAG, "Stop Gathering that had error finished");

                        //Maybe this line is useless
                        mSequenceGathering.initSequenceGathering(0, 0, 0);

                        getGathering(mSequenceGathering.getEllcieCallback());
                    } else {
                        Log.e(TAG, "cannot stop Gathering that encounters some errors");
                    }
                }));
            }
        } else if (mSequenceGathering.getSequenceCounter() != sequenceId) {
            Log.w(TAG, "invalid sequence");
            mSequenceGathering.incrementErrorCounter();
            if ((mSequenceGathering.getErrorCounter() % 20) == 1) {
                // restart sequence
                mCmdRunnable.addCommand(new GlassCommand(new CommandFallGatheringError(mSequenceGathering.getSequenceCounter()), COMMAND_WRITE_TIMEOUT, response -> {
                    if (response.isValid()) {
                        Log.d(TAG, "restartSequence Gathering ok");
                    } else {
                        Log.e(TAG, "error in restartSequence Gathering");
                    }
                }));

            } else if (mSequenceGathering.getErrorCounter() >= 40) {
                Log.e(TAG, "Too many errors, stop");
                mCmdRunnable.addCommand(new GlassCommand(new CommandFallGatheringStop(), COMMAND_WRITE_TIMEOUT, response -> {
                    if (response.isValid()) {
                        Log.d(TAG, "Stop Gathering that had error finished");

                        //Maybe this line is useless
                        mSequenceGathering.initSequenceGathering(0, 0, 0);

                        getGathering(mSequenceGathering.getEllcieCallback());
                    } else {
                        Log.e(TAG, "cannot stop Gathering that encounters some errors");
                    }
                }));
            }
        } else if (mSequenceGathering.getByteList() != null && mSequenceGathering.incrementSequenceCounter() == mSequenceGathering.getNbFrame()) {
            mSequenceGathering.resetErrorCounter();
            mSequenceGathering.addData(dataByte);

            mCmdRunnable.addCommand(new GlassCommand(new CommandFallGatheringStop(), COMMAND_WRITE_TIMEOUT, response -> {
            }));

            mCmdRunnable.addCommand(new GlassCommand(new CommandFallGatheringConfirm(), COMMAND_WRITE_TIMEOUT, response -> {
            }));

            if (mSequenceGathering.isValidSize()) {
                mSequenceGathering.done();
            } else {
                Log.e(TAG, "total size got is different thant the one expected, we reset operation");
                mCmdRunnable.addCommand(new GlassCommand(new CommandFallGatheringStop(), COMMAND_WRITE_TIMEOUT, response -> {
                    if (response.isValid()) {
                        Log.d(TAG, "Stop Gathering that had error finished");

                        //Maybe this line is useless
                        mSequenceGathering.initSequenceGathering(0, 0, 0);

                        getGathering(mSequenceGathering.getEllcieCallback());
                    } else {
                        Log.e(TAG, "cannot stop Gathering that encounters some errors");
                    }
                }));
            }

        } else if (mSequenceGathering.getByteList() != null) {
            mSequenceGathering.resetErrorCounter();
            mSequenceGathering.addData(dataByte);
        }
    }

    private void managePrevention(byte[] dataValue) {
        byte computeCheckSum = ChecksumUtils.computeChecksum(Arrays.copyOfRange(dataValue, 1, 21));
        final ByteBuffer bb = ByteBuffer.allocate(dataValue.length)
                .order(ByteOrder.BIG_ENDIAN)
                .put(dataValue);

        short sequenceId = bb.getShort(18);
        short crc = bb.get(0);
        byte[] dataByte = Arrays.copyOfRange(dataValue, 1, 18);

        if (computeCheckSum != crc) {
            Log.w(TAG, "invalid crc");
            mSequencePrevention.incrementErrorCounter();
            if ((mSequencePrevention.getErrorCounter() % 20) == 1) {
                // restart sequence

                mCmdRunnable.addCommand(new GlassCommand(new CommandFallPreventionError(mSequencePrevention.getSequenceCounter()), COMMAND_WRITE_TIMEOUT, response -> {
                    if (response.isValid()) {
                        Log.d(TAG, "restartSequence Prevention ok");
                    } else {
                        Log.e(TAG, "error in restartSequence Prevention");
                    }
                }));

            } else if (mSequencePrevention.getErrorCounter() >= 40) {
                Log.e(TAG, "Too many errors, stop");

                mCmdRunnable.addCommand(new GlassCommand(new CommandFallPreventionStop(), COMMAND_WRITE_TIMEOUT, response -> {
                    if (response.isValid()) {
                        Log.d(TAG, "Stop Prevention that had error finished");

                        //Maybe this line is useless
                        mSequencePrevention.initSequencePrevention(0, 0);

                        getPrevention(mSequencePrevention.getEllcieCallback());
                    } else {
                        Log.e(TAG, "cannot stop Prevention that encounters some errors");
                    }
                }));
            }
        } else if (mSequencePrevention.getSequenceCounter() != sequenceId) {
            Log.w(TAG, "invalid sequence");
            mSequencePrevention.incrementErrorCounter();
            if ((mSequencePrevention.getErrorCounter() % 20) == 1) {
                // restart sequence
                mCmdRunnable.addCommand(new GlassCommand(new CommandFallGatheringError(mSequencePrevention.getSequenceCounter()), COMMAND_WRITE_TIMEOUT, response -> {
                    if (response.isValid()) {
                        Log.d(TAG, "restartSequence Prevention ok");
                    } else {
                        Log.e(TAG, "error in restartSequence Prevention");
                    }
                }));

            } else if (mSequencePrevention.getErrorCounter() >= 40) {
                Log.e(TAG, "Too many errors, stop");
                mCmdRunnable.addCommand(new GlassCommand(new CommandFallPreventionStop(), COMMAND_WRITE_TIMEOUT, response -> {
                    if (response.isValid()) {
                        Log.d(TAG, "Stop Prevention that had error finished");

                        //Maybe this line is useless
                        mSequencePrevention.initSequencePrevention(0, 0);

                        getPrevention(mSequencePrevention.getEllcieCallback());
                    } else {
                        Log.e(TAG, "cannot stop Prevention that encounters some errors");
                    }
                }));
            }
        } else if (mSequencePrevention.getByteList() != null && mSequencePrevention.incrementSequenceCounter() == mSequencePrevention.getNbFrame()) {
            mSequencePrevention.resetErrorCounter();
            mSequencePrevention.addData(dataByte);

            mCmdRunnable.addCommand(new GlassCommand(new CommandFallPreventionStop(), COMMAND_WRITE_TIMEOUT, response -> {
            }));

            mCmdRunnable.addCommand(new GlassCommand(new CommandFallPreventionConfirm(), COMMAND_WRITE_TIMEOUT, response -> {
            }));

            if (mSequencePrevention.isValidSize()) {
                mSequencePrevention.done();
            } else {
                Log.e(TAG, "total size got is different thant the one expected, we reset operation");
                mCmdRunnable.addCommand(new GlassCommand(new CommandFallPreventionStop(), COMMAND_WRITE_TIMEOUT, response -> {
                    if (response.isValid()) {
                        Log.d(TAG, "Stop Prevention that had error finished");

                        //Maybe this line is useless
                        mSequencePrevention.initSequencePrevention(0, 0);

                        getPrevention(mSequencePrevention.getEllcieCallback());
                    } else {
                        Log.e(TAG, "cannot stop Prevention that encounters some errors");
                    }
                }));
            }

        } else if (mSequencePrevention.getByteList() != null) {
            mSequencePrevention.resetErrorCounter();
            mSequencePrevention.addData(dataByte);
        }
    }


    @Override
    public void onFotaEvent(@NonNull @NotNull BluetoothDevice device, @NonNull @NotNull FotaEvent event) {
        Log.d(TAG, "GOT IT");

    }

    @Override
    public void onFotaImage(@NonNull @NotNull BluetoothDevice device, @NonNull @NotNull ImageData image) {
        Log.d(TAG, "GOT IT");

    }

    private void clearDatas() {
        mDisconnectedSerialNumber = mGlasses.getSerial();
        mIsReady.postValue(false);

        if (mCmdRunnable != null) {
            mCmdRunnable.clear();
            mCmdRunnable.stop();
        }

        mWarning.postValue(WarningCode.WARNING_TEMP_OK);
        mTapsEnable.postValue(true);

        //For Serenity app, doing this call means reset fall information in case of loosong BLE connection in comment for now
        /*mFallInfo.clear();
        mFallData.postValue(mFallInfo);*/

        mGlasses.clear();
        mGlassesData.postValue(mGlasses);

//        mSensorsInfo.clear();
//        mSensorsData.postValue(mSensorsInfo);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        mClazz = (Class) intent.getExtras().get(clazzName);
//        if (mEventsBroadcastReceiveradcast == null) {
//            mEventsBroadcastReceiveradcast = new KillEllcieBroadcastReceiver();
//            registerReceiver(mEventsBroadcastReceiveradcast, new IntentFilter(KillEllcieBroadcastReceiver.INTENT_FILTER_VALUE));
//        }


        if (mClazz.getPackage().getName().contains("fall")) {
            mFallAlertFeature = new FallAlertFeature(this, mBinder);
            mFallAlertFeature.setListenerAlertEngaged(delaySecond -> {
                Log.d(TAG, "SHOW CONFIRMATION PAGE");
                final Intent broadcast = new Intent(ServiceConstant.BROADCAST_FALL_STATUS);
                broadcast.putExtra(ServiceConstant.FALL_VALUE, ServiceConstant.FallStateEnum.ALERT_ENGAGED.toString());
                broadcast.putExtra(ServiceConstant.FALL_COUNTDOWN, delaySecond);

                if (mFallCallback != null) {
                    mFallCallback.done(broadcast);
                } else {
                    LocalBroadcastManager.getInstance(EHBleForegroundService.this).sendBroadcast(broadcast);
                }
            });

            mFallAlertFeature.setListenerNoAlertConfirmed(event -> {
                Log.d(TAG, "SHOW WAITING FOR HELP PAGE");
                final Intent broadcast = new Intent(ServiceConstant.BROADCAST_FALL_STATUS);
                if (event.getRescueState().equals(RescueEvent.State.VITARIS_ACK)) {
                    broadcast.putExtra(ServiceConstant.FALL_VALUE, ServiceConstant.FallStateEnum.ALERT_CONFIRMED.toString());
                    broadcast.putExtra(ServiceConstant.FALL_RESCUE_STATE, event.getRescueState());
                    broadcast.putExtra(ServiceConstant.FALL_VITARIS_ACK_REASON, event.getVitarisAckReason());
                    broadcast.putExtra(ServiceConstant.FALL_VITARIS_ACK_HELPER, event.getVitarisAckHelperDisplayName());
                } else {
                    broadcast.putExtra(ServiceConstant.FALL_VALUE, ServiceConstant.FallStateEnum.ALERT_CONFIRMED.toString());
                    broadcast.putExtra(ServiceConstant.FALL_RESCUE_HELPER, event.getHelperDisplayName());
                    broadcast.putExtra(ServiceConstant.FALL_RESCUE_STATE, event.getRescueState());
                }

                if (mFallCallback != null) {
                    mFallCallback.done(broadcast);
                } else {
                    LocalBroadcastManager.getInstance(EHBleForegroundService.this).sendBroadcast(broadcast);
                }
            });

            mFallAlertFeature.setListenerNoAlert(done -> {
                Log.d(TAG, "SHOW DASHBOARD PAGE");
                final Intent broadcast = new Intent(ServiceConstant.BROADCAST_FALL_STATUS);
                broadcast.putExtra(ServiceConstant.FALL_VALUE, ServiceConstant.FallStateEnum.NO_ALERT.toString());
                if (mFallCallback != null) {
                    mFallCallback.done(broadcast);
                } else {
                    LocalBroadcastManager.getInstance(EHBleForegroundService.this).sendBroadcast(broadcast);
                }
            });

            mFallAlertFeature.start();
            mFallAlertFeature.appStarted();


        } else {
            if (mFallAlertFeature != null) {
                mFallAlertFeature.stop();
                mFallAlertFeature = null;
            }
        }


        Notification notification = createNotificationForForegroundService(this,
                getString(R.string.service_notif_title), "Démarrage du service Ellcie Healthy", mClazz);
        //startForegroundService(mClazz);

        startForeground(NOTIFICATION_ID, notification);


        return START_NOT_STICKY;
    }

    public Notification createNotificationForForegroundService(Context context, String title, String text, Class clazz) {
        NotificationCompat.Builder builderNotification = createNotificationBuilder(context, title, text, clazz, NotificationCompat.PRIORITY_MIN);
        //In comment for now since the stop service from notification bar is not okay in all terms
      /*  Intent intent = new Intent(KillEllcieBroadcastReceiver.INTENT_FILTER_VALUE);
        intent.putExtra(KillEllcieBroadcastReceiver.CLASS, clazz);
        PendingIntent hide = PendingIntent.getBroadcast(context, 456, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        builderNotification.addAction(0, context.getString(R.string.close_ellcie_app_text), hide);*/
        return builderNotification.build();
    }

    public static final String NOTIFICATION_HIGH_PRIORITY_CHANNEL_ID = "Notification_Ellcie_High_Importance";
    public static final String NOTIFICATION_DEFAULT_PRIORITY_CHANNEL_ID = "Notification_Ellcie_Default_Importance";
    public static final String NOTIFICATION_CHANNEL_NAME = "Ellcie Healthy";
    private static final int NOTIFICATION_LIGHT = Color.RED;
    private static final int TIME_LIGHT_ON = 3000;
    private static final int TIME_LIGHT_OFF = 3000;


    private static NotificationCompat.Builder createNotificationBuilder(Context context, String title, String text, Class clazz, int priority) {
        Bitmap iconEllcie = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);


        String notificationChannelId = NOTIFICATION_DEFAULT_PRIORITY_CHANNEL_ID;
        if (priority >= NotificationCompat.PRIORITY_HIGH && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannelId = NOTIFICATION_HIGH_PRIORITY_CHANNEL_ID;
        }


        NotificationCompat.Builder builder;
        builder = new NotificationCompat.Builder(context, notificationChannelId)
                .setSmallIcon(R.drawable.ic_logo_ellcie_healthy)
                .setLargeIcon(iconEllcie) //@FIX cannot find icon on oneplus 3
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text)) //exendable text, to show the text in his integrality
                .setContentTitle(title)
                .setContentText(text)
                .setLights(NOTIFICATION_LIGHT, TIME_LIGHT_ON, TIME_LIGHT_OFF) //set light
                .setAutoCancel(true)
                .setPriority(priority);

        builder.setVibrate(new long[]{0L}); // Passing null here silently fails

        if (clazz != null) { //check here to avoid crash
            // Creates an explicit intent for an Activity in your app
            Intent notificationIntent = new Intent(context, clazz);
            notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(context,
                    (int) System.currentTimeMillis(), notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(pendingIntent);
        }

        return builder;
    }

    public static class StreamingListener {
        @JvmField
        private final Intent mIntent;
        @JvmField
        private final SensorDataConsumer mConsumer;

        public StreamingListener(@NonNull Intent intent, @NonNull SensorDataConsumer consumer) {
            this.mIntent = intent;
            this.mConsumer = consumer;
        }

        public Intent getmIntent() {
            return mIntent;
        }

        public SensorDataConsumer getmConsumer() {
            return mConsumer;
        }
    }

    public class EHBinder extends LocalBinder {
        private final ArrayList<StreamingListener> mStreamingListeners = new ArrayList<>();

        public LiveData<Integer> getBatteryInfo() {
            return mBatteryLevel;
        }

        public LiveData<Boolean> isCharging() {
            return mIsCharging;
        }

        public LiveData<GlassesInfo> getGlassesInfo() {
            return mGlassesData;
        }

        public LiveData<SensorsInfo> getSensorsInfo() {
            return mSensorsData;
        }

        public LiveData<TripInfo> getTripInfo() {
            return mTripData;
        }

        public LiveData<FallInfo> getFallInfo() {
            return mFallData;
        }

        public LiveData<Boolean> isTapEnable() {
            return mTapsEnable;
        }

        public LiveData<WarningCode> getWarningInfo() {
            return mWarning;
        }

        public LiveData<ShutdownReason> getShutDown() {
            return mShutDown;
        }

        public LiveData<Boolean> isReady() {
            return mIsReady;
        }

        public LiveData<SilentModeReason> isSilentMode() {
            return mIsSilentMode;
        }

        public boolean addSensorStreamingListener(@NonNull Intent intent, @NonNull final SensorDataConsumer consumer) {
            for (StreamingListener listener : mStreamingListeners) {
                if ((listener.mIntent == intent) && (listener.mConsumer == consumer)) {
                    Log.w(TAG, "consumer for " + consumer.getSensorType() + " already added");
                    return false;
                }
            }

            mStreamingListeners.add(new StreamingListener(intent, consumer));
            Log.d(TAG, "add consumer for " + consumer.getSensorType() + " (" + mStreamingListeners.size() + ")");
            return true;
        }

        public boolean addSensorStreamingListenerAndStart(@NonNull Intent intent, @NonNull final SensorDataConsumer consumer) {
            if (addSensorStreamingListener(intent, consumer)) {
                if (!consumer.isRunning()) {
                    consumer.start();
                }
                return true;
            }

            return false;
        }

        public boolean addSensorStreamingListenerAndStart(@NonNull Intent intent, @NonNull final ArrayList<SensorDataConsumer<Object>> consumerList) {
            for (SensorDataConsumer currentConsumer : consumerList) {
                if (!addSensorStreamingListener(intent, currentConsumer)) {
                    return false;
                } else {
                    currentConsumer.start();
                }
            }

            return true;
        }


        public void delAllSensorStreamingListener(@NonNull Intent intent) {
            for (int i = 0; i < mStreamingListeners.size(); ) {
                StreamingListener listener = mStreamingListeners.get(i);

                if (listener.mIntent == intent) {
                    Log.d(TAG, "removing consumer for " + listener.mConsumer.getSensorType());
                    //mBleManager.getMeasureService().delSensorDataConsumer(listener.mConsumer);
                    mStreamingListeners.remove(i);
                } else {
                    ++i;
                }
            }
        }

        public boolean shutdown() {
            return EHBleForegroundService.this.shutdown();
        }

        public void startTripWithStreaming(@NonNull ArrayList<SensorType> sensors, boolean disableAlgo) {
            EHBleForegroundService.this.startTrip(sensors, disableAlgo);
        }


        public boolean stopTrip(final boolean updateAlgoState) {
            return EHBleForegroundService.this.stopTrip(false, updateAlgoState);
        }

        public boolean toggleTap() {
            return EHBleForegroundService.this.toggleTap();
        }

        public boolean localizeMe() {
            return EHBleForegroundService.this.localizeMe();
        }

        public void setStreamingFolderUri(@NonNull Uri uri) {
            EHBleForegroundService.this.setRootStreamingUri(uri);
        }

        @SuppressWarnings("unused")
        public void unsetStreamingFolderUri() {
            EHBleForegroundService.this.setRootStreamingUri(null);
        }

        public void setGlassesConfig(@NonNull GlassesConfig config) {
            EHBleForegroundService.this.setGlassesConfig(config);
        }

        public String getSerialNumber() {
            return EHBleForegroundService.this.mGlasses.getSerial();
        }

        public boolean engageSosFall() {
            return EHBleForegroundService.this.sosFall();
        }

        public boolean cancelSosFallAlert() {
            return EHBleForegroundService.this.cancelSosFallAlert();
        }

        public boolean cancelFallAlert() {
            return EHBleForegroundService.this.cancelFallAlert();
        }

        public boolean confirmAlert() {
            return EHBleForegroundService.this.confirmFallAlert();
        }

        public boolean enableFall() {
            return EHBleForegroundService.this.enableFall();
        }

        public boolean disableFall() {
            return EHBleForegroundService.this.disableFall();
        }


        public boolean confirmSosFall() {
            return EHBleForegroundService.this.confirmSosAlert();
        }

        public boolean setAlarmVolume(Integer buzzerVolume) {
            return EHBleForegroundService.this.setAlarmVolum(buzzerVolume);
        }

        public boolean setAlarmLuminosity(Integer lightIntensity) {
            return EHBleForegroundService.this.setAlarmLuminosity(lightIntensity);
        }

        public boolean setNotifLuminosity(Integer lightItensity) {
            return EHBleForegroundService.this.setNotifLuminosity(lightItensity);
        }

        public boolean setAlwaysAlert(boolean alwaysAlert) {
            return EHBleForegroundService.this.setAlwaysAlert(alwaysAlert);
        }

        @SuppressWarnings("UnusedReturnValue")
        public boolean setNotifVolum(int volum) {
            return EHBleForegroundService.this.setNotifVolum(volum);
        }

        public boolean setCallNotification() {
            return EHBleForegroundService.this.setComandIncomingCall();
        }

        public void setAlgoSensitivity(int sensitivity) {
            EHBleForegroundService.this.setAlgoSensitivity(sensitivity);
        }

        public void getBestMeanValue(GlassCommand.GlassCommandCallback callback) {
            EHBleForegroundService.this.getBestMeanValue(callback);
        }

        @SuppressWarnings({"UnusedReturnValue", "unused"})
        public boolean setFullConfig(int buzzerVolume, int buzzerVolumeNotification, int lightIntensity, int lightNotification, boolean sosGlasses, int sensibility) {
            return EHBleForegroundService.this.setFullConfig(buzzerVolume, buzzerVolumeNotification, lightIntensity, lightNotification, sosGlasses, sensibility);
        }

        @SuppressWarnings("UnusedReturnValue")
        public boolean playStreetAlarm() {
            return EHBleForegroundService.this.playStreetAlarm();
        }

        public boolean streetlabUnmuteAlarm() {
            return EHBleForegroundService.this.streetlabUnmuteAlarm();
        }

        @SuppressWarnings("UnusedReturnValue")
        public boolean sendDebugRisk(byte level) {
            return EHBleForegroundService.this.sendDebugRisk(level);
        }


        @SuppressWarnings("UnusedReturnValue")
        public boolean startOpticianTest() {
            return EHBleForegroundService.this.startOpticianTest();
        }

        public void stopOpticianTest(GlassCommand.GlassCommandCallback callback) {
            EHBleForegroundService.this.stopOpticianTest(callback);
        }

        public void setStreamDataConsumer(StreamDataConsumer consumer) {
            EHBleForegroundService.this.setStreamDataConsumer(consumer);
        }

        public MeasureService getMeasureService() {
            return EHBleForegroundService.this.mBleManager.getMeasureService();
        }

        public boolean muteGlasses() {
            return EHBleForegroundService.this.muteGlasses();
        }

        @SuppressWarnings("UnusedReturnValue")
        public boolean unmuteGlasses() {
            return EHBleForegroundService.this.unmuteGlasses();
        }

        public void getDriverGlassesLog(EllcieCommonCallbackGetGeneric<ArrayList<LogV2>> cb) {
            final ArrayList<LogV2> logList = new ArrayList<>();
            EHBleForegroundService.this.getDriverGlassesLog(cb, logList);
        }

        public void startOta(String fileDir, EllcieCommonCallbackGetBoolean cbOtaStarted, EllcieCommonOtaFinishCallback cbOtaCompleted, EllcieCommonOtaProgressCallback cbOtaProgress) {
            EHBleForegroundService.this.startOta(fileDir, cbOtaStarted, cbOtaCompleted, cbOtaProgress);
        }

        public void continueOta(EllcieCommonOtaFinishCallback cbOtaCompleted, EllcieCommonOtaProgressCallback cbOtaProgress) {
            EHBleForegroundService.this.continueOta(cbOtaCompleted, cbOtaProgress);
        }

        public void getGathering(EllcieCommonCallbackGetGeneric<SequenceGathering> cbFinished) {
            EHBleForegroundService.this.getGathering(cbFinished);
        }

        public void getPrevention(EllcieCommonCallbackGetGeneric<SequencePrevention> cbFinished) {
            EHBleForegroundService.this.getPrevention(cbFinished);
        }

        public boolean helperAcquittal() {
            return EHBleForegroundService.this.helperAcquittal();
        }

        public String getDeviceName() {
            return super.getDeviceName();

        }

        @Nullable
        public Class getClazz() {
            return mClazz;
        }

        public int getCurrentOta() {
            if (mBleManager.getFotaService() != null) {
                return mBleManager.getFotaService().getPercentageUploadCompleted();
            }
            return 0;
        }

        public void setFallConfig(boolean sosEnabled) {
            EHBleForegroundService.this.setFallConfig(sosEnabled);
        }

        public void resetOtaError() {
            if (mBleManager.getFotaService() != null) {
                mBleManager.getFotaService().notifyGlassesOtaError();
            }
        }

        public void getFallStatus() {
            EHBleForegroundService.this.getFallStatus();
        }

        public void getBatteryLevel() {
            EHBleForegroundService.this.getBatteryLevel();
        }

        @Nullable
        public String getLastDisconnectedSerialNumber() {
            return EHBleForegroundService.this.getDisconnectedSerialNumber();
        }


        public FallAlertFeature getFallAlertFeature() {
            return mFallAlertFeature;
        }


        public void setFallCallback(EllcieCommonCallbackGetGeneric<Intent> cb) {
            mFallCallback = cb;
        }

        public EllcieCommonCallbackGetGeneric<Intent> getFallCallback() {
            return mFallCallback;
        }

        public void stopService() {
            EHBleForegroundService.this.stopService();
        }

        public void onKill(Context context){EHBleForegroundService.this.onKill(context);}
    }
}
