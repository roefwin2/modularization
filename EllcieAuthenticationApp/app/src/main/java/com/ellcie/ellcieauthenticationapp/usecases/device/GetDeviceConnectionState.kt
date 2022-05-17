package com.ellcie.ellcieauthenticationapp.usecases.device

import com.ellcie.ellcieauthenticationapp.network.repositories.BleRepositoryImpl
import javax.inject.Inject

class GetDeviceConnectionState @Inject constructor(
    private val bleRepositoryImpl: BleRepositoryImpl
) {

    fun invoke() = bleRepositoryImpl.bleDeviceConnection
}