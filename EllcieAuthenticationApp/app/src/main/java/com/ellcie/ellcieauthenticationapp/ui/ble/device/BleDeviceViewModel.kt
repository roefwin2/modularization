package com.ellcie.ellcieauthenticationapp.ui.ble.device

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ellcie.ellcieauthenticationapp.network.repositories.BleRepositoryImpl
import com.ellcie.ellcieauthenticationapp.usecases.trip.StartStreamingTripUsecase
import com.ellcie.nordicblelibrary.services.BleDeviceConnection
import com.ellcie.nordicblelibrary.services.EHBleForegroundServiceBis
import com.ellcie.nordicblelibrary.services.ServiceContainer
import com.ellcie.nordicblelibrary.services.ServiceReady
import com.ellcie.toolkitlibrary.resource.Resource
import com.ellcie_healthy.ble_library.ble.profile.command.data.SensorType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BleDeviceViewModel @Inject constructor(
    private val bleRepositoryImpl: BleRepositoryImpl,
    private val startStreamingTripUsecase: StartStreamingTripUsecase
) :
    ViewModel() {

    private val _deviceState: MutableStateFlow<DeviceScreen> =
        MutableStateFlow(
            DeviceScreen(
                "Prudence",
                Resource.Loading,
                Resource.Loading,
                Resource.Loading,
                Resource.Loading
            )
        )
    val deviceState: StateFlow<DeviceScreen> get() = _deviceState

    fun getBatteryLevel() {
        viewModelScope.launch {
            bleRepositoryImpl.getBatteryLevel().collect {
                _deviceState.value = _deviceState.value.copy(batteryLevel = Resource.Success(it))
            }
        }
    }

    fun getChargingState() {
        viewModelScope.launch {
            bleRepositoryImpl.getChargingState().collect {
                _deviceState.value = _deviceState.value.copy(isCharging = Resource.Success(it))
            }
        }
    }

    fun localizeMe() {
       val sensorList =  arrayListOf(SensorType.EYE_SENSOR_LEFT_RIGHT,SensorType.TEMPERATURE)
        viewModelScope.launch {
            startStreamingTripUsecase.invoke(true, sensorList,true).collect {
                _deviceState.value = _deviceState.value.copy(localizeMeState = it)
            }
        }
    }

    fun disconnectToDevice() {
        viewModelScope.launch {
            bleRepositoryImpl.disconnectDevice().collect {
                _deviceState.value = _deviceState.value.copy(disconnectState = it)
            }
        }
    }
}


data class DeviceScreen(
    val name: String,
    val batteryLevel: Resource<Int>,
    val isCharging: Resource<Boolean>,
    val localizeMeState: Resource<String>,
    val disconnectState: Resource<String>
)
