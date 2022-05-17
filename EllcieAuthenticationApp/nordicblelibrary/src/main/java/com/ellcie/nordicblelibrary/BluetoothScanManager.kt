package com.ellcie.nordicblelibrary

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.os.Build
import android.os.ParcelUuid
import androidx.annotation.RequiresApi
import com.ellcie.nordicblelibrary.utils.BleConstants.ELLCIE_CONTROL_S
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class BluetoothScanManager@Inject constructor(bluetoothAdapter: BluetoothAdapter) {
    //TODO DI
    @RequiresApi(Build.VERSION_CODES.M)
    private val settings: ScanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
        .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
        .setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT) // Refresh the devices list every second
        // use report delay to 0 because some devices doesn't support other delay
        .setReportDelay(0) // Hardware filtering has some issues on selected devices
        // Samsung S6 and S6 Edge report equal value of RSSI for all devices. In this app we ignore the RSSI.
        .build()

    lateinit var scanCallback: ScanCallback
    private val bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner

    val state = callbackFlow {
        scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                super.onScanResult(callbackType, result)
                result?.let { scanResult ->
                    if (isEllcieDevice(scanResult.device, scanResult.scanRecord?.serviceUuids))
                        trySend(scanResult.device)
                }
            }

            override fun onScanFailed(errorCode: Int) {
                super.onScanFailed(errorCode)
            }

            override fun onBatchScanResults(results: MutableList<ScanResult>?) {
                super.onBatchScanResults(results)
            }

        }

        awaitClose { }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun startScan() {
        try {
            bluetoothLeScanner.startScan(null, settings, scanCallback)
        } catch (e: Exception) {

        }
    }

    fun stopScan() {
        try {
            bluetoothLeScanner.stopScan(scanCallback)
        } catch (e: Exception) {

        }
    }

    private fun isEllcieDevice(device: BluetoothDevice, uuidsList: List<ParcelUuid>?): Boolean {
        if (device.name == null || uuidsList == null) {
            return false
        }
        return uuidsList.any { ELLCIE_CONTROL_S == it.uuid }
    }

}