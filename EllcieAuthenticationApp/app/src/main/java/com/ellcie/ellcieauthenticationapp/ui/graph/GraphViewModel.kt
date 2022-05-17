package com.ellcie.ellcieauthenticationapp.ui.graph

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ellcie.ellcieauthenticationapp.usecases.sensors.GetSensorsDataUsecase
import com.ellcie.ellcieauthenticationapp.usecases.trip.StartTripUsecase
import com.ellcie.toolkitlibrary.actionstate.ActionState
import com.ellcie.toolkitlibrary.resource.Resource
import com.ellcie_healthy.ble_library.ble.profile.command.data.SensorType
import com.ellcie_healthy.ble_library.ble.profile.measure.data.SensorData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GraphViewModel @Inject constructor(
    private val getSensorsDataUsecase: GetSensorsDataUsecase,
    private val startTripUsecase: StartTripUsecase
) :
    ViewModel() {
    private val _graphState: MutableStateFlow<GraphScreen> =
        MutableStateFlow(GraphScreen(SensorData(SensorType.EYE_SENSOR_LEFT_RIGHT, 10L, 2.0),ActionState.NOT_STARTED))
    val graphState: StateFlow<GraphScreen>
        get() = _graphState


    fun getSensorsData() {
        viewModelScope.launch(Dispatchers.Default) {
            getSensorsDataUsecase.invoke().collect {
                _graphState.value = _graphState.value.copy(sensorData = it)
            }
        }
    }

    fun stopTrip(){
        viewModelScope.launch {
            startTripUsecase.invoke(false).collect{
               _graphState.value = _graphState.value.copy(stopTripAction = when(it){
                    is Resource.Error -> ActionState.ERROR
                    Resource.Loading -> ActionState.PENDING
                    is Resource.Success -> ActionState.SUCCESS
                })
            }
        }
    }
}

data class GraphScreen(
    val sensorData: SensorData<Double>,
    val stopTripAction :ActionState
)