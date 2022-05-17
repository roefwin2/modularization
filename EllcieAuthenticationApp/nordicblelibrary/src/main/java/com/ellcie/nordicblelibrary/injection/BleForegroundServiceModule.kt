package com.ellcie.nordicblelibrary.injection

import com.ellcie.nordicblelibrary.services.*
import com.ellcie_healthy.ble_library.ble.service.BleForegroundService
import com.ellcie_healthy.ble_library.ble.service.EHBleForegroundService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class BleForegroundServiceModule {

    @Binds
    abstract fun bindEHBleForegroundServiceReadyActivityBis(
        ehBleForegroundActivityReadyImpl: EhBleForegroundActivityReadyImpl
    ): BleForegroundServiceReadyActivityBis<ServiceContainer>

    @Binds
    abstract fun bindServiceContainer(
        serviceReady: ServiceReady
    ):ServiceContainer

    @Binds
    abstract fun bindNotServiceReadyContainer(
        notServiceReady: NotReadyService
    ):ServiceContainer

    @Binds
    abstract fun bindBleForegroundService(
        ehBleForegroundServiceBis: EHBleForegroundServiceBis
    ):BleForegroundService

    @Binds
    abstract fun bindLocalBinder(
        EHBinderBis: EHBleForegroundServiceBis.EHBinderBis
    ):BleForegroundService.LocalBinder

}