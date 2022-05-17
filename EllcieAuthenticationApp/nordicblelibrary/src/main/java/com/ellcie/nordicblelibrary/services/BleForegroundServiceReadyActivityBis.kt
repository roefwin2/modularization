package com.ellcie.nordicblelibrary.services

import android.app.Service
import android.bluetooth.BluetoothDevice
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.ellcie.toolkitlibrary.resource.Resource
import com.ellcie_healthy.ble_library.adapter.DiscoveredBluetoothDevice
import com.ellcie_healthy.ble_library.ble.service.BleForegroundService
import com.ellcie_healthy.ble_library.ble.service.BleForegroundService.EXTRA_DEVICE
import com.ellcie_healthy.ble_library.ble.service.BleForegroundService.EXTRA_DEVICE_ADDRESS
import com.ellcie_healthy.ble_library.ble.service.EHBleForegroundService
import com.ellcie_healthy.common.utils.com.ellcie_healthy.common.loggers.Logger.Companion.d
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import no.nordicsemi.android.ble.livedata.state.ConnectionState.Disconnected
import no.nordicsemi.android.ble.observer.ConnectionObserver
import java.lang.Exception
import java.lang.IllegalArgumentException
import javax.inject.Inject


abstract class BleForegroundServiceReadyActivityBis<E : ServiceContainer> :
    AppCompatActivity() {


    private val TAG: String =
        BleForegroundServiceReadyActivityBis::class.java.simpleName
    private val _service: MutableStateFlow<ServiceContainer> = MutableStateFlow(NotReadyService)
    val service: StateFlow<ServiceContainer> get() = _service


    var mServiceIntent: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // In onInitialize method a final class may register local broadcast receivers that will listen for events from the service
        onInitialize(savedInstanceState)
        // The onCreateView class should... create the view
        onCreateView(savedInstanceState)

        // View is ready to be used
        onViewCreated(savedInstanceState)
    }


    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            //TODO  optimieze recreate service with observer
            val bleService: E = ServiceReady((service as BleForegroundService.LocalBinder),BleDeviceConnection.DeviceDisconnected) as E
            _service.value = bleService
            d(TAG, "Activity bound to the service")
            mServiceIntent?.let {
                onServiceBound(it, bleService)
            }
            if (bleService is ServiceReady) {
                bleService.binder.connectionState?.observe(
                    this@BleForegroundServiceReadyActivityBis,
                    { state ->
                        when (state?.state) {
                            no.nordicsemi.android.ble.livedata.state.ConnectionState.State.CONNECTING -> {
                                onDeviceConnecting(bleService.binder.bluetoothDevice)
                            }
                            no.nordicsemi.android.ble.livedata.state.ConnectionState.State.INITIALIZING -> {
                                onDeviceInitializing(bleService.binder.bluetoothDevice)
                            }
                            no.nordicsemi.android.ble.livedata.state.ConnectionState.State.READY -> {
                                onDeviceConnected(
                                    bleService.binder.bluetoothDevice
                                )
                            }
                            no.nordicsemi.android.ble.livedata.state.ConnectionState.State.DISCONNECTING -> {
                                var reason = ConnectionObserver.REASON_TERMINATE_LOCAL_HOST
                                if (state is Disconnected) {
                                    reason = state.reason
                                }
                                onDeviceDisconnected(bleService.binder.bluetoothDevice, reason)
                            }
                            no.nordicsemi.android.ble.livedata.state.ConnectionState.State.DISCONNECTED -> {
                                bleService.binder.bluetoothDevice?.let {
                                    onDeviceDisconnecting(it)
                                }
                            }
                        }
                    })
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            _service.value = NotReadyService
            mServiceIntent = null
            onServiceUnbound()
        }
    }


    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
        try {
            // We don't want to perform some operations (e.g. disable Battery Level notifications)
            // in the service if we are just rotating the screen. However, when the activity will
            // disappear, we may want to disable some device features to reduce the battery
            // consumption.
            if (service.value is ServiceReady) {
                (service.value as ServiceReady).binder.setActivityIsChangingConfiguration(
                    isChangingConfigurations
                )
            }

            /*onServiceUnbound();
            unbindService(serviceConnection);

            mService = null;

            Logger.d(TAG, "Activity unbound from the service");
            mServiceIntent = null;*/
        } catch (e: IllegalArgumentException) {
            // do nothing, we were not connected to the sensor
        }
    }

    override fun onDestroy() {
        //This try catch is  for an exception that can occure when user kills the application
        //I don't understand why but the call to ondestroy leads to an illegalstateexception on the fragmentmanager
        try {
            super.onDestroy()
        } catch (ignored: Exception) {
        }
    }


    /**
     * You may do some initialization here. This method is called from [.onCreate] before the view was created.
     */
    protected open fun onInitialize(savedInstanceState: Bundle?) {
        // empty default implementation
    }

    /**
     * Called from [.onCreate]. This method should build the activity UI, i.e. using [.setContentView].
     * Use to obtain references to views. Connect/Disconnect button, the device name view are manager automatically.
     *
     * @param savedInstanceState contains the data it most recently supplied in [.onSaveInstanceState].
     * Note: **Otherwise it is null**.
     */
    protected abstract fun onCreateView(savedInstanceState: Bundle?)

    /**
     * Called after the view has been created.
     *
     * @param savedInstanceState contains the data it most recently supplied in [.onSaveInstanceState].
     * Note: **Otherwise it is null**.
     */
    protected open fun onViewCreated(savedInstanceState: Bundle?) {
        // empty default implementation
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
    }

    open fun connect(address: String, clazz: Class<*>, context: Context) {
        //TODO add try catch
        val service = Intent(context, getServiceClass())
        service.putExtra(EXTRA_DEVICE_ADDRESS, address)
        service.putExtra(BleForegroundService.clazzName, clazz)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(service)
        } else {
            context.startService(service)
        }
        context.bindService(service, serviceConnection, BIND_AUTO_CREATE)
    }

    open fun connect(discoveredBluetoothDevice: DiscoveredBluetoothDevice, clazz: Class<*>?) {
        val service = Intent(this, clazz)
        service.putExtra(EXTRA_DEVICE_ADDRESS, discoveredBluetoothDevice.address)
        service.putExtra(EXTRA_DEVICE, discoveredBluetoothDevice)
        service.putExtra(clazz?.name, clazz)
        bindService(service, serviceConnection, 0)
    }

    open fun unbindService(context: Context) {
        //TODO add try catch
        if (service.value is ServiceReady) {
            (service.value as ServiceReady).binder.setActivityIsChangingConfiguration(
                isChangingConfigurations
            )
            onServiceUnbound()
            val service = Intent(context, getServiceClass())
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                context.stopService(service)
            }
            context.unbindService(serviceConnection)
            _service.value = NotReadyService
            mServiceIntent = null
            //stop service also
        }

    }

    protected open fun getServiceClass(): Class<out BleForegroundService?>? {
        return EHBleForegroundServiceBis::class.java
    }

    protected open fun onServiceBound(intent: Intent?, binder: E) {}
    protected open fun onServiceUnbound() {}
    protected open fun onDeviceConnecting(device: BluetoothDevice) {
        // empty default implementation
    }

    protected open fun onDeviceInitializing(device: BluetoothDevice) {
        // empty default implementation
    }

    abstract fun onDeviceConnected(device: BluetoothDevice)

    protected open fun onDeviceDisconnecting(device: BluetoothDevice) {
        // empty default implementation
    }

    protected open fun onDeviceDisconnected(device: BluetoothDevice, reason: Int) {
        // empty default implementation
    }
}


open class EhBleForegroundActivityReadyImpl @Inject constructor() :
    BleForegroundServiceReadyActivityBis<ServiceContainer>() {
    //TODO may be use state flow to observe in onDeviceConnected (isCharging, etc...)

    val bleDeviceConnection: Flow<ServiceContainer> get() = service

    @Inject
    lateinit var context: Context

    override fun onStart() {
        super.onStart()
        /*
         * If the service has not been started before, the following lines will not start it.
         * However, if it's running, the Activity will bind to it and notified via serviceConnection.
         */

        /*
         * If the service has not been started before, the following lines will not start it.
         * However, if it's running, the Activity will bind to it and notified via serviceConnection.
         */
        // We pass 0 as a flag so the service will not be created if not exists.
        // We pass 0 as a flag so the service will not be created if not exists.
        mServiceIntent = Intent(context, getServiceClass())
        //context.bindService(mServiceIntent, serviceConnection, BIND_AUTO_CREATE)
    }

    fun disconnectBinder(context: Context) {
        getServiceClass().newInstance()?.onKill(context)
        //stop service also
    }

    override fun getServiceClass(): Class<out BleForegroundService?> {
        return EHBleForegroundServiceBis::class.java
    }

    override fun onServiceBound(intent: Intent?, binder: ServiceContainer) {
        Log.d("", "")
    }

    override fun onServiceUnbound() {
    }


    override fun onDeviceDisconnecting(device: BluetoothDevice) {
        super.onDeviceDisconnecting(device)
    }

    override fun onDeviceConnecting(device: BluetoothDevice) {
        super.onDeviceConnecting(device)
    }

    override fun onDeviceInitializing(device: BluetoothDevice) {
        super.onDeviceInitializing(device)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(savedInstanceState: Bundle?) {
    }

    override fun onDeviceConnected(device: BluetoothDevice) {
        Log.d("", "")
    }

}

sealed class ServiceContainer {
}

data class ServiceReady(
    val binder: BleForegroundService.LocalBinder,
    val bleDeviceConnection: BleDeviceConnection
) : ServiceContainer()

object NotReadyService : ServiceContainer()


sealed class Device
data class DeviceConnected(
    val serviceContainer: ServiceContainer
) : Device()

object DeviceDisconnected