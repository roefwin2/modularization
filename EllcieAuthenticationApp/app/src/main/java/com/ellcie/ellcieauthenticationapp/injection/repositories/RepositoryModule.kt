package com.ellcie.ellcieauthenticationapp.injection.repositories

import android.content.Context
import com.ellcie.backofficelibrary.backofficemanager.BackOfficeManagerImpl
import com.ellcie.ellcieauthenticationapp.injection.components.AuthUserComponent
import com.ellcie.ellcieauthenticationapp.network.repositories.AuthenticationRepositoryImpl
import com.ellcie.ellcieauthenticationapp.network.repositories.BackOfficeRepositoryImpl
import com.ellcie.ellcieauthenticationapp.network.repositories.BleRepositoryImpl
import com.ellcie.ellcieauthenticationapp.network.repositories.FirebaseRepositoryImpl
import com.ellcie.ellcieauthenticationapp.usecases.RefreshTokenUseCase
import com.ellcie.ellcieauthenticationlibrary.authmanager.EllcieAuthManagerImpl
import com.ellcie.ellciefirebaselibrary.firebasemanager.EllcieFirebaseDataManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(AuthUserComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideFirebaseRepositoryImpl(
        ellcieFirebaseDataManager: EllcieFirebaseDataManager
    ): FirebaseRepositoryImpl {
        return FirebaseRepositoryImpl(ellcieFirebaseDataManager)
    }

    @Provides
    @Singleton
    fun provideBackOfficeRepositoryImpl(
        backOfficeManagerImpl: BackOfficeManagerImpl,
        refreshTokenUseCase: RefreshTokenUseCase
    ): BackOfficeRepositoryImpl {
        return BackOfficeRepositoryImpl(backOfficeManagerImpl,refreshTokenUseCase)
    }

    @Provides
    @Singleton
    fun provideAuthenticationRepositoryImpl(
        ellcieAuthManagerImpl: EllcieAuthManagerImpl
    ): AuthenticationRepositoryImpl {
        return AuthenticationRepositoryImpl(ellcieAuthManagerImpl)
    }
}