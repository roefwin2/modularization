package com.ellcie.ellcieauthenticationapp.injection.library

import com.ellcie.backofficelibrary.backofficemanager.BackOfficeManagerImpl
import com.ellcie.ellcieauthenticationapp.injection.components.AuthUserComponent
import com.ellcie.ellcieauthenticationapp.injection.components.AuthUserScope
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn


@Module
@InstallIn(AuthUserComponent::class)
object EllcieBackOfficeManagerModule {

    @AuthUserScope
    @Provides
    fun provideEllcieBacOfficeManager(
    ): BackOfficeManagerImpl {
        return BackOfficeManagerImpl()
    }
}