package com.ellcie.nordicblelibrary.services

import android.app.Notification
import android.app.Service
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.lifecycleScope
import com.ellcie.nordicblelibrary.activity.NordicBleMainActivity
import com.ellcie.nordicblelibrary.models.Connected
import com.ellcie.nordicblelibrary.models.DeviceConnection
import com.ellcie.nordicblelibrary.models.Disconnected
import com.ellcie.nordicblelibrary.models.Initializing
import com.ellcie.nordicblelibrary.repository.DeviceInitializedRepository
import com.ellcie.toolkitlibrary.resource.Resource
import com.ellcie_healthy.ble_library.R
import com.ellcie_healthy.ble_library.ble.models.GlassesConfig
import com.ellcie_healthy.ble_library.ble.models.streaming.SensorDataCallback
import com.ellcie_healthy.ble_library.ble.models.streaming.SensorDataConsumer
import com.ellcie_healthy.ble_library.ble.profile.BleWriteCharacteristic
import com.ellcie_healthy.ble_library.ble.profile.EHBleManager
import com.ellcie_healthy.ble_library.ble.profile.battery.BatteryManagerCallbacks
import com.ellcie_healthy.ble_library.ble.profile.command.CommandService
import com.ellcie_healthy.ble_library.ble.profile.command.callback.CommandEventCallback
import com.ellcie_healthy.ble_library.ble.profile.command.data.SensorType
import com.ellcie_healthy.ble_library.ble.profile.command.data.cmd.*
import com.ellcie_healthy.ble_library.ble.profile.command.data.event.*
import com.ellcie_healthy.ble_library.ble.profile.fota.FotaCallbacks
import com.ellcie_healthy.ble_library.ble.profile.fota.data.FotaEvent
import com.ellcie_healthy.ble_library.ble.profile.fota.data.ImageData
import com.ellcie_healthy.ble_library.ble.profile.generic.information.DeviceInformationCallbacks
import com.ellcie_healthy.ble_library.ble.profile.measure.MeasureCallbacks
import com.ellcie_healthy.ble_library.ble.profile.measure.data.RiskData
import com.ellcie_healthy.ble_library.ble.profile.measure.data.SensorData
import com.ellcie_healthy.ble_library.ble.service.BleForegroundService
import com.ellcie_healthy.ble_library.ble.service.CommandsRunnable
import com.ellcie_healthy.ble_library.ble.service.EHBleForegroundService
import com.ellcie_healthy.ble_library.ble.service.GlassCommand
import com.ellcie_healthy.ble_library.ble.service.GlassCommand.GlassCommandCallback
import com.ellcie_healthy.ble_library.ble.service.GlassCommand.PreGlassCommandCallback
import com.ellcie_healthy.ble_library.ble.utils.Utils
import com.ellcie_healthy.ble_library.ble.utils.createNotificationBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.internal.ChannelFlow
import kotlinx.coroutines.launch
import no.nordicsemi.android.ble.data.Data
import java.lang.Exception
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

/**
 * Class used for the client Binder. The Binder object is responsible for returning an instance
 * of "MyService" to the client.
 */


class MyBoundService : BleForegroundService() {

    private val _sensors = MutableStateFlow(SensorData(SensorType.EYE_SENSOR_LEFT_RIGHT, 10L, 2.0))
    private val sensorDataCallback = object : SensorDataCallback<Double> {
        override fun onSensorData(data: SensorData<Double>) {
            _sensors.value = data
        }
    }


    companion object {
        private val _deviceState: MutableStateFlow<DeviceConnection> =
            MutableStateFlow(Disconnected(""))
        private val _battery: MutableStateFlow<Int> = MutableStateFlow(-25)
        private val _isCharging: MutableStateFlow<Boolean> = MutableStateFlow(false)
        private const val NOTIFICATION_ID = 519
        private val mStreamingListeners: java.util.ArrayList<EHBleForegroundService.StreamingListener> =
            java.util.ArrayList<EHBleForegroundService.StreamingListener>()
        private val mSensorDataConsumer: SensorDataConsumer<Double> = SensorDataConsumer(
            SensorDataConsumer.SensorDataConsumerType.FG,
            "lol",
            SensorType.ANY,
            1000
        )
    }

    private val mCmdRunnable: CommandsRunnable by lazy {
        CommandsRunnable(mBleManager)
    }

    private lateinit var intent: Intent

    private val mGlassesConfig = GlassesConfig(5, 5, 50, 50, false, false, false, -1)

    private val binder: MyBinder = MyBinder()

    //TODO create helper for callbacks
    private val deviceInformationCallbacks =
        object : DeviceInformationCallbacks {
            override fun onUnexpectedError(device: BluetoothDevice, status: Int, message: String) {
            }

            override fun onFirmwareRevision(device: BluetoothDevice, firmware: String) {

            }

            override fun onModelNumber(device: BluetoothDevice, model: String) {

            }

            override fun onSerialNumber(device: BluetoothDevice, serial: String) {

            }
        }

    private val batteryManagerCallbacks = object : BatteryManagerCallbacks {
        override fun onBatteryLevel(device: BluetoothDevice, batteryLevel: Int) {
            when (_deviceState.value) {
                is Initializing -> _battery.value = batteryLevel
                else -> _deviceState.value
            }
        }

        override fun onBatteryPowerState(
            device: BluetoothDevice,
            plug: Boolean?,
            charging: Boolean?
        ) {
            when (_deviceState.value) {
                is Initializing -> charging?.let {
                    _isCharging.value = it
                }
                else -> _deviceState.value
            }
        }
    }

    private val commandEventCallback = object : CommandEventCallback {
        override fun onEventLocalizeMyPhone(event: EventDataLocalize) {
        }

        override fun onEventTripStateChange(event: EventDataTrip) {
        }

        override fun onEventWarning(event: EventDataWarning) {
        }

        override fun onEventTapsMode(event: EventDataTap) {

        }

        override fun onEventFallStateChange(event: EventDataFall) {
        }

        override fun onEventShutdown(event: EventDataShutdown) {
        }

        override fun onSilentMode(event: EventDataSilentMode) {
        }

        override fun onUnimplementedEvent(code: EventCode, event: EventData) {
        }
    }

    private val measureCallback =
        object : MeasureCallbacks {

            override fun onDebugData(device: BluetoothDevice, data: Data) {
            }

            override fun onDataGatheringData(device: BluetoothDevice, data: Data) {
            }

            override fun onHumidityValue(device: BluetoothDevice, humidity: Int) {
            }

            override fun onWornValue(device: BluetoothDevice, duration: Int) {
            }

            override fun onStepValue(device: BluetoothDevice, steps: Int) {

            }

            override fun onPressureValue(device: BluetoothDevice, pressure: Int) {
            }

            override fun onRiskValue(device: BluetoothDevice, risk: RiskData) {
            }

            override fun onTemperatureValue(device: BluetoothDevice, temperature: Int) {
            }
        }


    private val fotaCallbacks = object : FotaCallbacks {
        override fun onFotaEvent(device: BluetoothDevice, event: FotaEvent) {
        }

        override fun onFotaImage(device: BluetoothDevice, image: ImageData) {
        }
    }

    private fun addSensorStreamingListener(intent: Intent): Boolean {
        for (listener in mStreamingListeners) {
            if (listener.getmIntent() == intent && listener.getmConsumer() == mSensorDataConsumer) {
                Log.w(
                    "MyBoundService",
                    "consumer for " + mSensorDataConsumer.consumerName.toString() + " already added"
                )
                return false
            }
        }
        mStreamingListeners.add(
            EHBleForegroundService.StreamingListener(
                intent,
                mSensorDataConsumer
            )
        )
        return mBleManager.addSensorDataConsumer(mSensorDataConsumer)
    }

    private fun addSensorStreamingListenerAndStart(
        intent: Intent
    ): Boolean {
        if (addSensorStreamingListener(intent)) {
            if (!mSensorDataConsumer.isRunning) {
                mSensorDataConsumer.start()
            }
            return true
        }
        return false
    }

    override fun onCreate() {
        //Use startForeground in onCreate https://stackoverflow.com/a/46449975
        super.onCreate()
        val notification: Notification = createNotificationForForegroundService(
            this,
            getString(R.string.service_notif_title),
            "Démarrage du service Ellcie Healthy",
            NordicBleMainActivity::class.java
        )
        startForeground(NOTIFICATION_ID, notification)
        val executor: Executor = Executors.newSingleThreadExecutor()
        executor.execute(mCmdRunnable)
        mSensorDataConsumer.setCallback(sensorDataCallback)
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        intent?.let { intent1 ->
            intent1.extras?.get(BleForegroundService::clazzName.name)?.let {
            }
        }
        return Service.START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        this.intent = intent
        return binder
    }

    override fun onServiceRebind(intent: Intent?) {

    }

    override fun onServiceUnbind(intent: Intent?) {

    }

    override fun initializeManager(): EHBleManager {
        return EHBleManager(
            this,
            deviceInformationCallbacks,
            batteryManagerCallbacks,
            commandEventCallback,
            measureCallback
        )
    }

    override fun onDeviceInitializing() {
        super.onDeviceInitializing()
        _deviceState.value = Initializing(DeviceInitializedRepository(binder))
    }

    override fun onDeviceConnected(forceConfig: Boolean) {
        super.onDeviceConnected(forceConfig)
        sendInitCommands(forceConfig)
        enableFallCommand()
    }

    override fun onDeviceDisconnecting() {
        super.onDeviceDisconnecting()
    }

    override fun onDeviceDisconnected(reason: Int) {
        super.onDeviceDisconnected(reason)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun sendInitCommands(forceConfig: Boolean) {
        mCmdRunnable.addCommand(GlassCommand(CommandGetSilentModeNotif(),
            COMMAND_WRITE_TIMEOUT, {
                mBleManager.requestConnectionPriorityHigh()
            }) { response: CommandResponse ->
        })
        if (forceConfig) {
            mCmdRunnable.addCommand(
                GlassCommand(
                    CommandSetFullConfig(
                        mGlassesConfig.getAlarmVolume(),
                        mGlassesConfig.getNotifVolume(),
                        mGlassesConfig.getAlarmLuminosity(),
                        mGlassesConfig.getNotifLuminosity(),
                        mGlassesConfig.isSilentMode(),
                        TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()),
                        mGlassesConfig.isEnableSos(),
                        mGlassesConfig.isEnableFaiting()
                    ), COMMAND_WRITE_TIMEOUT
                )
            )
        }
        mCmdRunnable.addCommand(
            GlassCommand(
                CommandSetTimestamp(
                    TimeUnit.MILLISECONDS.toSeconds(
                        System.currentTimeMillis()
                    )
                ),
                COMMAND_WRITE_TIMEOUT
            )
        )
        mCmdRunnable.addCommand(GlassCommand(
            CommandGetTripStatus(), COMMAND_WRITE_TIMEOUT
        ) { response: CommandResponse ->
        })
        finalizeInit()
    }

    private fun finalizeInit() {
        mBleManager.requestConnectionPriorityBalanced()
        //Push directly on the main thread
        //mIsReady.setValue(true)
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

    //Commands
    suspend fun localizeMe() = callbackFlow {
        trySend(Resource.Loading)
        try {
            mCmdRunnable.addCommand(
                GlassCommand(
                    CommandLocalizeMe(), 2000
                ) { response: CommandResponse ->
                    if (!response.isValid) {
                        trySend(Resource.Error("unable to send localize"))
                    } else {
                        trySend(Resource.Success("OK"))
                    }
                })
        } catch (e: Exception) {
            trySend(Resource.Error(e.toString()))
        }
        awaitClose { }
    }

    private fun enableFallCommand() {
        mCmdRunnable.addCommand(
            GlassCommand(
                CommandFallCommands(CommandFallCommands.FallCommandCode.ENABLE_FALL), 2000
            ) { response: CommandResponse -> })
    }

    suspend fun disableFallCommand() = callbackFlow {
        trySend(Resource.Loading)
        try {
            mCmdRunnable.addCommand(
                GlassCommand(
                    CommandFallCommands(CommandFallCommands.FallCommandCode.DISABLE_FALL), 2000
                ) { response: CommandResponse ->
                    if (!response.isValid) {
                        trySend(Resource.Error("unable to disable fall"))
                    } else {
                        trySend(Resource.Success("OK"))
                    }
                })
        } catch (e: Exception) {
            trySend(Resource.Error(e.toString()))
        }
        awaitClose { }
    }

    suspend fun engageSosFall() = callbackFlow {
        trySend(Resource.Loading)
        try {
            mCmdRunnable.addCommand(
                GlassCommand(
                    CommandFallCommands(CommandFallCommands.FallCommandCode.ENGAGE_SOS), 2000
                ) { response: CommandResponse ->
                    if (!response.isValid) {
                        trySend(Resource.Error("unable to engage sos"))
                    } else {
                        trySend(Resource.Success("OK"))
                    }
                })
        } catch (e: Exception) {
            trySend(Resource.Error(e.toString()))
        }
        awaitClose { }
    }

    suspend fun cancelSosFall() = callbackFlow {
        trySend(Resource.Loading)
        try {
            mCmdRunnable.addCommand(
                GlassCommand(
                    CommandFallCommands(CommandFallCommands.FallCommandCode.CANCEL_SOS), 2000
                ) { response: CommandResponse ->
                    if (!response.isValid) {
                        trySend(Resource.Error("unable to cancel sos"))
                    } else {
                        trySend(Resource.Success("OK"))
                    }
                })
        } catch (e: Exception) {
            trySend(Resource.Error(e.toString()))
        }
        awaitClose { }
    }

    suspend fun disableAlgoCommand(disable: Boolean) = callbackFlow {
        trySend(Resource.Loading)
        val cmdAlg = CommandDisableAlgo(disable)
        try {
            mCmdRunnable.addCommand(
                GlassCommand(
                    cmdAlg, 4000
                ) { response: CommandResponse ->
                    if (!response.isValid) {
                        trySend(Resource.Error("unable to $disable algo"))
                    } else {
                        trySend(Resource.Success("OK"))
                    }
                })
        } catch (e: Exception) {
            trySend(Resource.Error(e.toString()))
        }
        awaitClose { }
    }

    suspend fun setUpStreamingCommand(sensorsList: ArrayList<SensorType>) = callbackFlow {
        trySend(Resource.Loading)
        val cmdStreaming = CommandSetStreaming(sensorsList)
        try {
            mCmdRunnable.addCommand(
                GlassCommand(
                    cmdStreaming, 4000
                ) { response: CommandResponse ->
                    if (!response.isValid) {
                        trySend(Resource.Error(response.responseCode.name))
                    } else {
                        trySend(Resource.Success("OK"))
                    }
                })
        } catch (e: Exception) {
            trySend(Resource.Error(e.toString()))
        }
        awaitClose { }
    }


    fun setTripStatusCommand(tripEnable: Boolean) = toResource(CommandSetTripStatus(tripEnable))

    /** Helper to wrap ble command with Resource flow */
    private fun toResource(
        commandResponse: CommandResponse,
    ): Flow<Resource<String>> = callbackFlow {
        trySend(Resource.Loading)
            try {
                mCmdRunnable.addCommand(
                    GlassCommand(
                        commandResponse, 4000
                    ) { response: CommandResponse ->
                        if (!response.isValid) {
                            trySend(Resource.Error(response.responseCode.name))
                        } else {
                            addSensorStreamingListenerAndStart(
                                intent
                            )
                            trySend(Resource.Success("OK"))
                        }
                    })
        } catch (e: Exception) {
           trySend(Resource.Error(e.toString()))
        }
        awaitClose { mCmdRunnable.stop() }
    }


    inner class MyBinder : LocalBinder() {

        // Return this instance of MyService so clients can call public methods
        val service: MyBoundService
            get() =// Return this instance of MyService so clients can call public methods
                this@MyBoundService


        val deviceState: StateFlow<DeviceConnection>
            get() = _deviceState

        val battery: StateFlow<Int>
            get() = _battery

        val isCharging: StateFlow<Boolean>
            get() = _isCharging

        val sensors: StateFlow<SensorData<Double>>
            get() = _sensors

        suspend fun lokalizeMe() = localizeMe()

        suspend fun disableFall() = disableFallCommand()

        suspend fun engageSos() = engageSosFall()

        suspend fun cancelSos() = cancelSosFall()

        suspend fun disableAlgo(disable: Boolean) = disableAlgoCommand(disable)

        suspend fun setupStreaming(sensorsList: ArrayList<SensorType>) =
            setUpStreamingCommand(sensorsList)

        suspend fun setTripStatus(tripEnable: Boolean) = setTripStatusCommand(tripEnable)
    }
}