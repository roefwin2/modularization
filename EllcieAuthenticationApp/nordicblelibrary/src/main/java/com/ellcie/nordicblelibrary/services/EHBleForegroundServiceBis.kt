package com.ellcie.nordicblelibrary.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.os.LocaleList
import android.util.Log
import androidx.core.app.NotificationCompat
import com.ellcie_healthy.ble_library.R
import com.ellcie_healthy.ble_library.ble.profile.EHBleManager
import com.ellcie_healthy.ble_library.ble.profile.battery.BatteryManagerCallbacks
import com.ellcie_healthy.ble_library.ble.profile.command.callback.CommandEventCallback
import com.ellcie_healthy.ble_library.ble.profile.command.data.event.*
import com.ellcie_healthy.ble_library.ble.profile.fota.FotaCallbacks
import com.ellcie_healthy.ble_library.ble.profile.fota.data.FotaEvent
import com.ellcie_healthy.ble_library.ble.profile.fota.data.ImageData
import com.ellcie_healthy.ble_library.ble.profile.generic.information.DeviceInformationCallbacks
import com.ellcie_healthy.ble_library.ble.profile.measure.MeasureCallbacks
import com.ellcie_healthy.ble_library.ble.profile.measure.data.RiskData
import com.ellcie_healthy.ble_library.ble.service.BleForegroundService
import com.ellcie_healthy.ble_library.ble.utils.createNotification
import com.ellcie_healthy.ble_library.ble.utils.createNotificationBuilder
import kotlinx.coroutines.flow.*
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.ble.livedata.state.ConnectionState


open class EHBleForegroundServiceBis : BleForegroundService(), DeviceInformationCallbacks,BatteryManagerCallbacks,
    CommandEventCallback, MeasureCallbacks, FotaCallbacks {

    private val TAG = EHBleForegroundServiceBis::class.java.simpleName
    private val ACTION_DISCONNECT = "$TAG.ACTION_DISCONNECT"
    private val NOTIFICATION_ID = 519

    lateinit var mClazz: Class<*>


    protected val binder: EHBinderBis = EHBinderBis()


    private val _service: MutableStateFlow<ServiceContainer> = MutableStateFlow(NotReadyService)
    val service: StateFlow<ServiceContainer> get() = _service

    private val disconnectActionBroadcastReceiver: BroadcastReceiver =
        object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                Log.i(
                    TAG, "[Notification] Disconnect action pressed"
                )
                stopService()
            }
        }

    private val _isReady : MutableStateFlow<Boolean> = MutableStateFlow(false)


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        mClazz = (intent!!.extras!![clazzName] as Class<*>?)!!
        val notification: Notification = createNotificationForForegroundService(
            this,
            getString(R.string.service_notif_title), "Démarrage du service Ellcie Healthy", mClazz
        )
        startForeground(NOTIFICATION_ID, notification)
        return Service.START_NOT_STICKY
    }

    private fun createNotificationForForegroundService(
        context: Context,
        title: String,
        text: String,
        clazz: Class<*>?
    ): Notification {
        val builderNotification = createNotificationBuilder(
            context,
            title,
            text,
            clazz,
            NotificationCompat.PRIORITY_MIN
        )
        return builderNotification.build()
    }


    override fun onServiceRebind(intent: Intent?) {
        stopForegroundService()
    }

    override fun onServiceUnbind(intent: Intent?) {
        startForegroundService(mClazz)
    }


    override fun initializeManager(): EHBleManager {
        //mStreamDataConsumer = StreamDataConsumer(this, manager.measureService)
        return EHBleManager(this, this,this, this, this)
    }

    override fun onCreate() {
        super.onCreate()
        val filter = IntentFilter()
        filter.addAction(ACTION_DISCONNECT)
        registerReceiver(disconnectActionBroadcastReceiver, filter)
    }

    override fun shouldAutoConnect(): Boolean {
        return false
    }

    override fun onDestroy() {
//        Log.d(TAG, "onDestroy: STOP SERVICE");
        try {
            binder.disconnect()
        } catch (ignored: Exception) {

        }
        cancelNotification()
        try {
            unregisterReceiver(disconnectActionBroadcastReceiver)
        } catch (err: IllegalArgumentException) {
            err.printStackTrace()
        }
        super.onDestroy()
    }


    override fun onBind(intent: Intent): IBinder? {
        return super.onBind(intent)
    }

    /*override fun onDeviceConnecting() {
        super.onDeviceConnecting()
        _deviceState.value = BleDeviceConnection.DeviceConnected(bluetoothDevice.address)
        updateNotification(mClazz)
    }

    override fun onDeviceInitializing() {
        super.onDeviceInitializing()
        _deviceState.value = BleDeviceConnection.DeviceConnected(bluetoothDevice.address)
        updateNotification(mClazz)
    }

    override fun onDeviceConnected(forceConfig: Boolean) {
        super.onDeviceConnected(forceConfig)
        _deviceState.value = BleDeviceConnection.DeviceReady(batteryLevel = -1,isCharging = false)
        _isReady.value = true
        updateNotification(mClazz)
    }

    override fun onDeviceDisconnected(reason: Int) {
        super.onDeviceDisconnected(reason)
        _deviceState.value = BleDeviceConnection.DeviceDisconnected
        _isReady.value = false
        updateNotification(mClazz)
    }

    override fun onDeviceDisconnecting() {
        super.onDeviceDisconnecting()
        updateNotification(mClazz)
    }*/

    override fun stopWhenDisconnected(): Boolean {
        return false
    }

    private fun startForegroundService(clazz: Class<*>) {
        // when the activity closes we need to show the notification that user is connected to the peripheral sensor
        // We start the service as a foreground service as Android 8.0 (Oreo) onwards kills any running background services
        val notification: Notification = createNotification(
            clazz,
            binder.connectionState.value?.state ?: ConnectionState.State.DISCONNECTED,
            bluetoothDevice.name,
            this
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //  startForeground(NOTIFICATION_ID, notification);
            val chan = NotificationChannel(
                "Ellcie service",
                "Driver service", NotificationManager.IMPORTANCE_NONE
            )
            chan.lightColor = Color.BLUE
            chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(chan)
            startForeground(NOTIFICATION_ID, notification)
        } else {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.notify(NOTIFICATION_ID, notification)
        }
    }

    /**
     * Stops the service as a foreground service
     */
    private fun stopForegroundService() {
        // when the activity rebinds to the service, remove the notification and stop the foreground service
        // on devices running Android 8.0 (Oreo) or above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true)
            cancelNotification()
        } else {
            cancelNotification()
        }
    }

    private fun updateNotification(clazz: Class<*>) {
        val nm =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(
            NOTIFICATION_ID,
            createNotification(
                clazz,
                binder.connectionState.value?.state ?: ConnectionState.State.DISCONNECTED,
                bluetoothDevice.name,
                this
            )
        )
    }

    private fun cancelNotification() {
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .cancel(NOTIFICATION_ID)
    }

    override fun onBatteryLevel(device: BluetoothDevice, batteryLevel: Int) {
       /* _deviceState.value = when(val currentState = _deviceState.value){
            is BleDeviceConnection.DeviceReady -> currentState.copy(batteryLevel = batteryLevel)
            else -> currentState
        }*/
    }

    override fun onBatteryPowerState(device: BluetoothDevice, plug: Boolean?, charging: Boolean?) {
        /*charging?.let {
            _deviceState.value = when(val currentState = _deviceState.value){
                is BleDeviceConnection.DeviceReady -> currentState.copy(isCharging = it)
                else -> currentState
            }
        }*/
    }

    override fun onUnexpectedError(device: BluetoothDevice, status: Int, message: String) {
        Log.d(TAG, "listener service");
    }

    override fun onFirmwareRevision(device: BluetoothDevice, firmware: String) {
        Log.d(TAG, "listener service");
    }

    override fun onModelNumber(device: BluetoothDevice, model: String) {
        Log.d(TAG, "listener service");
    }

    override fun onSerialNumber(device: BluetoothDevice, serial: String) {
        Log.d(TAG, "listener service");
    }

    override fun onEventLocalizeMyPhone(event: EventDataLocalize) {
        Log.d(TAG, "listener service");
    }

    override fun onEventTripStateChange(event: EventDataTrip) {
        Log.d(TAG, "listener service");
    }

    override fun onEventWarning(event: EventDataWarning) {
        Log.d(TAG, "listener service");
    }

    override fun onEventTapsMode(event: EventDataTap) {
        Log.d(TAG, "listener service");
    }

    override fun onEventFallStateChange(event: EventDataFall) {
        Log.d(TAG, "listener service");
    }

    override fun onEventShutdown(event: EventDataShutdown) {
        Log.d(TAG, "listener service");
    }

    override fun onSilentMode(event: EventDataSilentMode) {
        Log.d(TAG, "listener service");
    }

    override fun onUnimplementedEvent(code: EventCode, event: EventData) {
        Log.d(TAG, "listener service");
    }

    override fun onFotaEvent(device: BluetoothDevice, event: FotaEvent) {
        Log.d(TAG, "listener service");
    }

    override fun onFotaImage(device: BluetoothDevice, image: ImageData) {
        Log.d(TAG, "listener service");
    }

    override fun onDebugData(device: BluetoothDevice, data: Data) {
        Log.d(TAG, "listener service");
    }

    override fun onDataGatheringData(device: BluetoothDevice, data: Data) {
        Log.d(TAG, "listener service");
    }

    override fun onHumidityValue(device: BluetoothDevice, humidity: Int) {
        Log.d(TAG, "listener service");
    }

    override fun onWornValue(device: BluetoothDevice, duration: Int) {
        Log.d(TAG, "listener service");
    }

    override fun onStepValue(device: BluetoothDevice, steps: Int) {
        Log.d(TAG, "listener service");
    }

    override fun onPressureValue(device: BluetoothDevice, pressure: Int) {
        Log.d(TAG, "listener service");
    }

    override fun onRiskValue(device: BluetoothDevice, risk: RiskData) {
        Log.d(TAG, "listener service");
    }

    override fun onTemperatureValue(device: BluetoothDevice, temperature: Int) {
        Log.d(TAG, "listener service");
    }


    inner class EHBinderBis : LocalBinder() {

        //val deviceState: Flow<BleDeviceConnection> get() = this@EHBleForegroundServiceBis._deviceState
        val isReady : Flow<Boolean> get() = this@EHBleForegroundServiceBis._isReady
    }
}

sealed class BleDeviceConnection {
    data class DeviceReady(val batteryLevel: Int, val isCharging : Boolean) :
        BleDeviceConnection()
    data class DeviceConnected(val serialNumber: String) : BleDeviceConnection()
    object DeviceDisconnected : BleDeviceConnection()
}
