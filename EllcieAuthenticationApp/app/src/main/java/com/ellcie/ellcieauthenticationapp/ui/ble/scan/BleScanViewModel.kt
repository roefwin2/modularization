package com.ellcie.ellcieauthenticationapp.ui.ble.scan

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ellcie.ellcieauthenticationapp.network.repositories.BleRepositoryImpl
import com.ellcie.nordicblelibrary.EllcieBleManager
import com.ellcie.nordicblelibrary.ResearchBleManager
import com.ellcie.toolkitlibrary.resource.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
//TODO use use case for the computation
class BleScanViewModel @Inject constructor(
    private val researchBleManager: ResearchBleManager,
    private val bleRepositoryImpl: BleRepositoryImpl
) : ViewModel() {


    private val _bleScanState: MutableStateFlow<BleScanScreen> = MutableStateFlow(
        BleScanScreen(
            listOf(), Resource.Loading, Resource.Loading
        )
    )
    val bleScanState: StateFlow<BleScanScreen> get() = _bleScanState


    @RequiresApi(Build.VERSION_CODES.N)
    fun startScan() {
        viewModelScope.launch {
           researchBleManager.devices.collect { devices ->
                val list = _bleScanState.value.deviceList.toMutableSet()
                list.add(Pair(devices.address, devices.name))
                _bleScanState.value = _bleScanState.value.copy(deviceList = list.toList())
            }
        }
        researchBleManager.startScan()
    }

    fun connectToDevice(macAddress: String, clazz: Class<*>) {
        researchBleManager.stopScan()
        viewModelScope.launch {
            bleRepositoryImpl.connectDevice(macAddress, clazz).collect {
                _bleScanState.value = _bleScanState.value.copy(connectionState = it)
            }
        }
    }
    fun resetSate(){
        _bleScanState.value =  BleScanScreen(
            listOf(), Resource.Loading, Resource.Loading
        )
    }

    fun destroy() {
        //bleRepositoryImpl.destroyEhBle()
    }
}

data class BleScanScreen(
    var deviceList: List<Pair<String, String>>,
    val connectionState: Resource<String>,
    val localizeMeState: Resource<String>
)
