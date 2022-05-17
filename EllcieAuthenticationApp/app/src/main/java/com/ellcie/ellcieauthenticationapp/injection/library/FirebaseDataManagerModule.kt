package com.ellcie.ellcieauthenticationapp.injection.library

import com.ellcie.ellcieauthenticationapp.injection.components.AuthUserComponent
import com.ellcie.ellcieauthenticationapp.injection.components.AuthUserScope
import com.ellcie.ellciefirebaselibrary.firebasemanager.EllcieFirebaseDataManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn


@Module
@InstallIn(AuthUserComponent::class)
object FirebaseDataManagerModule {

    @Provides
    @AuthUserScope
    fun providesFirebaseDataManager(): EllcieFirebaseDataManager {
        return EllcieFirebaseDataManager()
    }
}