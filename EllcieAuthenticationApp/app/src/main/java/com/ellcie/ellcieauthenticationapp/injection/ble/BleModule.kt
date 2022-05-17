package com.ellcie.ellcieauthenticationapp.injection.ble

import android.content.Context
import com.ellcie.ellcieauthenticationapp.network.repositories.BleRepositoryImpl
import com.ellcie.nordicblelibrary.EllcieBleManager
import com.ellcie.nordicblelibrary.ResearchBleManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object BleModule {

    @Provides
    @Singleton
    fun provideBleRepositoryImpl(
        researchBleManager: ResearchBleManager,
        @ApplicationContext context: Context
    ): BleRepositoryImpl {
        return BleRepositoryImpl(researchBleManager,context)
    }
}