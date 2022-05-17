package com.ellcie.ellcieauthenticationapp.injection.activity

import com.ellcie.ellcieauthenticationapp.activity.DemoAppActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import javax.inject.Singleton

@Module
@InstallIn(ActivityComponent::class)
object ActivityModule {

    @Provides
    @Singleton
    fun provideDemoAppActivity() : DemoAppActivity = DemoAppActivity()
}