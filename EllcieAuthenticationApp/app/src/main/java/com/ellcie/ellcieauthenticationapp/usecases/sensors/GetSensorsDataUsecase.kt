package com.ellcie.ellcieauthenticationapp.usecases.sensors

import com.ellcie.ellcieauthenticationapp.network.repositories.BleRepositoryImpl
import com.ellcie_healthy.ble_library.ble.profile.measure.data.SensorData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetSensorsDataUsecase @Inject constructor(private val bleRepositoryImpl: BleRepositoryImpl) {

    fun invoke() = flow {
         bleRepositoryImpl.getSensorsData().collect {
            emit(it)
        }
    }
}