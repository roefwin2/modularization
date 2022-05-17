package com.ellcie.ellcieauthenticationapp.injection.usecases

import com.ellcie.ellcieauthenticationapp.injection.components.AuthUserComponent
import com.ellcie.ellcieauthenticationapp.injection.components.AuthUserScope
import com.ellcie.ellcieauthenticationapp.usecases.RefreshTokenUseCase
import com.ellcie.ellcieauthenticationlibrary.authmanager.EllcieAuthManagerImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn

@Module
@InstallIn(AuthUserComponent::class)
object UseCaseModule {

    @AuthUserScope
    @Provides
    fun providesRefreshTokenUseCase(
        ellcieAuthManagerImpl: EllcieAuthManagerImpl
    ): RefreshTokenUseCase {
        return RefreshTokenUseCase(ellcieAuthManagerImpl)
    }
}