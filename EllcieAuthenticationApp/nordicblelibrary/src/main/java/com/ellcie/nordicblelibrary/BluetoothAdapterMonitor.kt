package com.ellcie.nordicblelibrary

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Context.BLUETOOTH_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class BluetoothAdapterMonitor(private val context: Context) {
    @RequiresApi(Build.VERSION_CODES.M)
    val bluetoothAdapter: BluetoothAdapter = (context.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager).adapter

    /** Observable that allows monitoring the state of the bluetooth adapter
    The operations inside this callback flow are executed once it is collected and
    cleaned when no collections are active.*/
    @RequiresApi(Build.VERSION_CODES.M)
    val state: Flow<Boolean> = callbackFlow {
        // Retrieves the initial state of the bluetooth adapter
        trySend(bluetoothAdapter.isEnabled)

        // Subscribes to state changes in the bluetooth adapter
        val bluetoothBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when (val action = intent.action) {
                        BluetoothAdapter.ACTION_STATE_CHANGED -> {
                        handleStateChanged(this@callbackFlow, intent)
                    }
                    else -> {
                        Log.v(TAG, "Unsupported bluetooth action received: $action")
                    }
                }
            }
        }
        registerReceiver(bluetoothBroadcastReceiver)

        // Clean-up when flow is no longer observed
        awaitClose {
            cleanup(bluetoothBroadcastReceiver)
        }
    }

    private fun registerReceiver(broadcastReceiver: BroadcastReceiver) {
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        context.registerReceiver(broadcastReceiver, filter)
    }

    private fun cleanup(broadcastReceiver: BroadcastReceiver) {
        context.unregisterReceiver(broadcastReceiver)
    }

    private fun handleStateChanged(callbackFlow: ProducerScope<Boolean>, intent: Intent) {
        val state = intent.getIntExtra(
            BluetoothAdapter.EXTRA_STATE,
            BluetoothAdapter.ERROR
        )
        when (state) {
            BluetoothAdapter.STATE_ON -> {
                callbackFlow.trySend(true)
            }
            BluetoothAdapter.STATE_OFF -> {
                callbackFlow.trySend(false)
            }
            else -> {
                Log.v(TAG, "Unsupported state received: $state")
            }
        }
    }

}