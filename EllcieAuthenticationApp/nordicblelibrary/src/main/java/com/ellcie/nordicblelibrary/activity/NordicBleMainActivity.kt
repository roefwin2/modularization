package com.ellcie.nordicblelibrary.activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ellcie.nordicblelibrary.models.DeviceConnection
import com.ellcie.nordicblelibrary.models.Disconnected
import com.ellcie.nordicblelibrary.services.Device
import com.ellcie.nordicblelibrary.services.MyBoundService
import com.ellcie_healthy.ble_library.ble.service.BleForegroundService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NordicBleMainActivity @Inject constructor() : AppCompatActivity() {

    /**
     * Interface for getting the instance of binder from our service class
     * So client can get instance of our service class and can directly communicate with it.
     */
    private lateinit var service: BleForegroundService.LocalBinder
    private val _serviceState: MutableStateFlow<DeviceConnection> =
        MutableStateFlow(Disconnected(""))
    val serviceState: StateFlow<DeviceConnection>
        get() = _serviceState


    private val serviceConnection: ServiceConnection by lazy {
        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as BleForegroundService.LocalBinder
                this@NordicBleMainActivity.service = binder.service
                when (binder) {
                    is MyBoundService.MyBinder -> {
                        lifecycleScope.launch {
                            binder.deviceState.collect {
                                _serviceState.value = it
                            }
                        }
                    }
                    else -> throw IllegalArgumentException("binder not compatible type")
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                TODO("Not yet implemented")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    fun connectBleDevice(address: String, clazz: Class<*>, context: Context) {
        //TODO add try catch
        val service = Intent(context, MyBoundService::class.java)
        service.putExtra(BleForegroundService.EXTRA_DEVICE_ADDRESS, address)
        service.putExtra(BleForegroundService.clazzName, clazz)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(service)
        } else {
            context.startService(service)
        }
        context.bindService(service, serviceConnection, BIND_AUTO_CREATE)
    }

    fun disconnectBleDevice(context: Context) {
        // Unbinding to the service class
        unbindService(context)
    }

    /**
     * Used to bind to our service class
     */
    private fun bindService() {
        Intent(this, MyBoundService::class.java).also { intent ->
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    /**
     * Used to unbind and stop our service class
     */
    private fun unbindService(context: Context) {
        Intent(this, MyBoundService::class.java).also { intent ->
            context.unbindService(serviceConnection)
        }
    }

    override fun onStart() {
        super.onStart()
        // Binding to the service class
        bindService()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unbinding to the service class
        //unbindService()
    }
}

sealed class ServiceState
data class ServiceReadyState(val binder: BleForegroundService.LocalBinder) : ServiceState()
object NoServiceReadyState : ServiceState()