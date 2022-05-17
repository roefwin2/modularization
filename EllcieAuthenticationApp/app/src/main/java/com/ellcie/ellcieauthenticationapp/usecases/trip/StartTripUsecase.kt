package com.ellcie.ellcieauthenticationapp.usecases.trip

import com.ellcie.ellcieauthenticationapp.network.repositories.BleRepositoryImpl
import com.ellcie.toolkitlibrary.resource.Resource
import com.ellcie_healthy.ble_library.ble.profile.command.data.SensorType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class StartTripUsecase@Inject constructor(private val bleRepositoryImpl: BleRepositoryImpl) {

    suspend fun invoke(tripEnable : Boolean) : Flow<Resource<String>> {
        return bleRepositoryImpl.setTripStatus(tripEnable).map {
            it
        }
    }
}