package com.ellcie.ellcieauthenticationapp.usecases.trip

import com.ellcie.ellcieauthenticationapp.network.repositories.BleRepositoryImpl
import com.ellcie.ellcieauthenticationapp.usecases.algo.DisableAlgoUsecase
import com.ellcie.ellcieauthenticationapp.usecases.streaming.SetupStreamingUsecase
import com.ellcie.toolkitlibrary.resource.Resource
import com.ellcie_healthy.ble_library.ble.profile.command.data.SensorType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class StartStreamingTripUsecase@Inject constructor(
    private val disableAlgoUsecase: DisableAlgoUsecase,
    private val setupStreamingUsecase: SetupStreamingUsecase,
    private val startTripUsecase: StartTripUsecase
) {

    suspend fun invoke(disableAlgo : Boolean,sensorList : ArrayList<SensorType>,tripEnable : Boolean) : Flow<Resource<String>> =
        combine(
            disableAlgoUsecase.invoke(disableAlgo),
            setupStreamingUsecase.invoke(sensorList),
            startTripUsecase.invoke(tripEnable)
        ){ _disableResource,_setupStreamResource,_startTripResource ->
            val algo = when(_disableResource){
                is Resource.Error -> return@combine _disableResource
                Resource.Loading -> return@combine _disableResource
                is Resource.Success -> {}
            }
            val setup = when(_setupStreamResource){
                is Resource.Error -> return@combine _setupStreamResource
                Resource.Loading -> return@combine _setupStreamResource
                is Resource.Success -> {}
            }
            val startTrip = when(_startTripResource){
                is Resource.Error -> return@combine _startTripResource
                Resource.Loading -> return@combine _startTripResource
                is Resource.Success -> {}
            }

            Resource.Success("Trip with streaming successfully started")
        }
}