package com.ellcie.ellcieauthenticationapp.usecases.algo

import com.ellcie.ellcieauthenticationapp.network.repositories.BleRepositoryImpl
import com.ellcie.toolkitlibrary.resource.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DisableAlgoUsecase@Inject constructor(private val bleRepositoryImpl: BleRepositoryImpl) {

    suspend fun invoke(disable : Boolean) : Flow<Resource<String>> {
      return bleRepositoryImpl.disableAlgo(disable).map {
            it
        }
    }
}