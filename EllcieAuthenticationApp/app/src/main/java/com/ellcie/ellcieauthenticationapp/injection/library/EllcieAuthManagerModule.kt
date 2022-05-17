package com.ellcie.ellcieauthenticationapp.injection.library

import com.ellcie.ellcieauthenticationapp.injection.components.AuthUserComponent
import com.ellcie.ellcieauthenticationapp.injection.components.AuthUserScope
import com.ellcie.ellcieauthenticationlibrary.authmanager.EllcieAuthManagerImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn

@Module
@InstallIn(AuthUserComponent::class)
object EllcieAuthManagerModule {

    @AuthUserScope
    @Provides
    fun provideEllcieAuthManagerImpl(): EllcieAuthManagerImpl {
        return EllcieAuthManagerImpl()
    }
}