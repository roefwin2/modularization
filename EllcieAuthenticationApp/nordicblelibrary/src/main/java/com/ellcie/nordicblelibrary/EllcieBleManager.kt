package com.ellcie.nordicblelibrary

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.ellcie.nordicblelibrary.activity.NordicBleMainActivity
import com.ellcie.nordicblelibrary.models.Connected
import com.ellcie.nordicblelibrary.models.Initializing
import com.ellcie.toolkitlibrary.resource.Resource
import com.ellcie_healthy.ble_library.ble.profile.command.data.SensorType
import com.ellcie_healthy.ble_library.ble.profile.measure.data.SensorData
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import javax.inject.Inject

interface EllcieDeviceManager {

    suspend fun connectivityState()
    fun startScan()
    fun stopScan()
    fun connect(macAddress: String, clazz: Class<*>, context: Context): Flow<Resource<String>>
    fun localizeMe() : Flow<Resource<String>>
    fun getBatteryLevel(): Flow<Int>
    fun getChargingState() : Flow<Boolean>
    fun disconnect(context: Context): Flow<Resource<String>>
}

class EllcieBleManager @Inject constructor() : EllcieDeviceManager {

    @Inject
    internal lateinit var bluetoothScanManager: BluetoothScanManager

    @Inject
    internal lateinit var bluetoothAdapterMonitor: BluetoothAdapterMonitor

    @Inject
    internal lateinit var nordicBleMainActivity: NordicBleMainActivity

    private val _bluetoothState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val bluetoothState: Flow<Boolean> get() = _bluetoothState

    val devices: Flow<BluetoothDevice>
        get() = bluetoothScanManager.state

    val deviceConnection
        get() = Pair(
            when (nordicBleMainActivity.serviceState.value) {
                is Initializing -> true
                else -> false
            }, "ble device connected"
        )


    /** The current scan subscribing job that listens for detected devices */
    private var scanJob: Job? = null

    override suspend fun connectivityState() {
        bluetoothAdapterMonitor.state.collect {
            _bluetoothState.value = it
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun startScan() {
        bluetoothScanManager.startScan()
    }

    override fun stopScan() {
        bluetoothScanManager.stopScan()
    }

    override fun connect(macAddress: String, clazz: Class<*>, context: Context) = flow {
        emit(Resource.Loading)
        try {
            nordicBleMainActivity.connectBleDevice(macAddress, clazz, context)
            emit(Resource.Success("Success to connect to $macAddress"))
        } catch (e: Exception) {
            emit(Resource.Error(e.toString()))
        }
    }

    override fun disconnect(context: Context) = flow {
        emit(Resource.Loading)
        try {
            nordicBleMainActivity.disconnectBleDevice(context)
            emit(Resource.Success("Successfully disconnected "))
        } catch (e: Exception) {
            emit(Resource.Error(e.toString()))
        }
    }

    override fun localizeMe() = nordicBleMainActivity.serviceState.flatMapConcat { serviceState ->
        when (serviceState) {
            is Initializing -> serviceState.initializedRepository.lokalizeMe()
            else -> flowOf(Resource.Loading)
        }
    }

    override fun getBatteryLevel() = nordicBleMainActivity.serviceState.flatMapConcat { serviceState ->
        when (serviceState) {
            is Initializing -> serviceState.initializedRepository.battery
            else -> flowOf(-12)
        }
    }

    override fun getChargingState() = nordicBleMainActivity.serviceState.flatMapConcat { serviceState ->
        when (serviceState) {
            is Initializing -> serviceState.initializedRepository.isCharging
            else -> flowOf(false)
        }
    }

    suspend fun disableFall() = nordicBleMainActivity.serviceState.flatMapConcat { serviceState ->
        when (serviceState) {
            is Initializing -> serviceState.initializedRepository.disableFall()
            else -> flowOf(Resource.Loading)
        }
    }

    suspend fun engageSos() = nordicBleMainActivity.serviceState.flatMapConcat { serviceState ->
        when (serviceState) {
            is Initializing -> serviceState.initializedRepository.engageSos()
            else -> flowOf(Resource.Loading)
        }
    }

    suspend fun cancelSos() = nordicBleMainActivity.serviceState.flatMapConcat { serviceState ->
        when (serviceState) {
            is Initializing -> serviceState.initializedRepository.cancelSos()
            else -> flowOf(Resource.Loading)
        }
    }

}

class ResearchBleManager@Inject constructor() : EllcieDeviceManager {

    @Inject
    internal lateinit var bluetoothScanManager: BluetoothScanManager

    @Inject
    internal lateinit var bluetoothAdapterMonitor: BluetoothAdapterMonitor

    @Inject
    internal lateinit var nordicBleMainActivity: NordicBleMainActivity

    private val _bluetoothState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val bluetoothState: Flow<Boolean> get() = _bluetoothState

    val devices: Flow<BluetoothDevice>
        get() = bluetoothScanManager.state

    val deviceConnection
        get() = Pair(
            when (nordicBleMainActivity.serviceState.value) {
                is Initializing -> true
                else -> false
            }, "ble device connected"
        )


    override suspend fun connectivityState() {
        bluetoothAdapterMonitor.state.collect {
            _bluetoothState.value = it
        }
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun startScan() {
        bluetoothScanManager.startScan()
    }

    override fun stopScan() {
        bluetoothScanManager.stopScan()
    }

    override fun connect(macAddress: String, clazz: Class<*>, context: Context) = flow {
        emit(Resource.Loading)
        try {
            nordicBleMainActivity.connectBleDevice(macAddress, clazz, context)
            emit(Resource.Success("Success to connect to $macAddress"))
        } catch (e: Exception) {
            emit(Resource.Error(e.toString()))
        }
    }

    override fun disconnect(context: Context) = flow {
        emit(Resource.Loading)
        try {
            nordicBleMainActivity.disconnectBleDevice(context)
            emit(Resource.Success("Successfully disconnected "))
        } catch (e: Exception) {
            emit(Resource.Error(e.toString()))
        }
    }

    override fun localizeMe() = nordicBleMainActivity.serviceState.flatMapConcat { serviceState ->
        when (serviceState) {
            is Initializing -> serviceState.initializedRepository.lokalizeMe()
            else -> flowOf(Resource.Loading)
        }
    }

    override fun getBatteryLevel() = nordicBleMainActivity.serviceState.flatMapConcat { serviceState ->
        when (serviceState) {
            is Initializing -> serviceState.initializedRepository.battery
            else -> flowOf(-12)
        }
    }

    override fun getChargingState() = nordicBleMainActivity.serviceState.flatMapConcat { serviceState ->
        when (serviceState) {
            is Initializing -> serviceState.initializedRepository.isCharging
            else -> flowOf(false)
        }
    }

    suspend fun disableAlgo(disable : Boolean) = nordicBleMainActivity.serviceState.flatMapConcat { serviceState ->
        when (serviceState) {
            is Initializing -> serviceState.initializedRepository.disableAlgo(disable)
            else -> flowOf(Resource.Loading)
        }
    }

    fun setupStreaming(sensorsList: ArrayList<SensorType>) = nordicBleMainActivity.serviceState.flatMapConcat { serviceState ->
        when (serviceState) {
            is Initializing -> serviceState.initializedRepository.setUpStreaming(sensorsList)
            else -> flowOf(Resource.Loading)
        }
    }

    fun setTripStatus(tripEnable : Boolean) = nordicBleMainActivity.serviceState.flatMapConcat { serviceState ->
        when (serviceState) {
            is Initializing -> serviceState.initializedRepository.setTripStatus(tripEnable)
            else -> flowOf(Resource.Loading)
        }
    }

    fun getSensorData() = nordicBleMainActivity.serviceState.flatMapConcat { serviceState ->
        when (serviceState) {
            is Initializing -> serviceState.initializedRepository.sensors
            else -> flowOf(SensorData(SensorType.EYE_SENSOR_LEFT_RIGHT, 10L, 2.0))
        }
    }

}

