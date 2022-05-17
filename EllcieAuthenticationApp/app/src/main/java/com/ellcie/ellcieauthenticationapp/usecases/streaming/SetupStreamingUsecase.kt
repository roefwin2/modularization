package com.ellcie.ellcieauthenticationapp.usecases.streaming

import com.ellcie.ellcieauthenticationapp.network.repositories.BleRepositoryImpl
import com.ellcie.toolkitlibrary.resource.Resource
import com.ellcie_healthy.ble_library.ble.profile.command.data.SensorType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SetupStreamingUsecase@Inject constructor(private val bleRepositoryImpl: BleRepositoryImpl) {

    suspend fun invoke(sensorList : ArrayList<SensorType>) : Flow<Resource<String>> {
        return bleRepositoryImpl.setupStreaming(sensorList).map {
            it
        }
    }
}