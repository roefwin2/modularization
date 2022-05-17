package com.ellcie.ellcieauthenticationapp.injection.library

import com.ellcie.ellcieauthenticationapp.injection.components.AuthUserComponent
import com.ellcie.ellcieauthenticationapp.injection.components.AuthUserScope
import com.ellcie.nordicblelibrary.ResearchBleManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn

@Module
@InstallIn(AuthUserComponent::class)
object EllcieBleManagerModule {

    @Provides
    @AuthUserScope
    fun providesResearchBleManager(
    ): ResearchBleManager {
        return ResearchBleManager()
    }
}