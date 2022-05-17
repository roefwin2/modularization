package com.ellcie.nordicblelibrary.injection

import android.content.Context
import com.ellcie.nordicblelibrary.BluetoothAdapterMonitor
import com.ellcie.nordicblelibrary.BluetoothScanManager
import com.ellcie.nordicblelibrary.services.*
import com.ellcie_healthy.ble_library.ble.service.EHBleForegroundService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BluetoothManagerModule {

    @Provides
    @Singleton
    fun providesBluetoothAdapterMonitor(
        @ApplicationContext context: Context
    ): BluetoothAdapterMonitor {
        return BluetoothAdapterMonitor(context)
    }

    @Provides
    @Singleton
    fun provideBluetoothScanManager(
        bluetoothAdapterMonitor: BluetoothAdapterMonitor
    ): BluetoothScanManager {
        return BluetoothScanManager(bluetoothAdapterMonitor.bluetoothAdapter)
    }

    @Provides
    fun provideEHBleForegroundServiceImpl(
    ): EhBleForegroundActivityReadyImpl {
        return EhBleForegroundActivityReadyImpl()
    }

    @Provides
    fun provideEHBleForegroundService(
    ): EHBleForegroundService {
        return EHBleForegroundService()
    }

    @Provides
    fun provideEHBinder(
        ehBleForegroundService: EHBleForegroundService
    ): EHBleForegroundService.EHBinder {
        return ehBleForegroundService.EHBinder()
    }

    @Provides
    fun provideEHBleForegroundServiceBis(
    ): EHBleForegroundServiceBis {
        return EHBleForegroundServiceBis()
    }

    @Provides
    fun provideEHBinderBis(
        ehBleForegroundServiceBis: EHBleForegroundServiceBis
    ): EHBleForegroundServiceBis.EHBinderBis {
        return ehBleForegroundServiceBis.EHBinderBis()
    }

    @Provides
    fun provideServiceReady(
        ehBleForegroundServiceBis: EHBleForegroundServiceBis
    ): ServiceReady {
        return ServiceReady(ehBleForegroundServiceBis.EHBinderBis(),BleDeviceConnection.DeviceDisconnected)
    }

    @Provides
    fun provideNotReadyService(
    ): NotReadyService {
        return NotReadyService
    }

    @Provides
    fun provideContext(
        @ApplicationContext context: Context
    ): Context {
        return context
    }
}