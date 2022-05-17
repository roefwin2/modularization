package com.ellcie.ellcieauthenticationlibrary.injection

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.ellcie.ellcieauthenticationlibrary.BuildConfig
import com.ellcie.ellcieauthenticationlibrary.utils.Constants.CLIENT_ID
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.ResponseTypeValues
import net.openid.appauth.AuthorizationServiceConfiguration
import javax.inject.Singleton


/**
 * Module to get all the configuration for the use of the AppAuth library
 */
@Module
@InstallIn(SingletonComponent::class)
class AuthorizationServiceModule {
    @Provides
    @Singleton
    fun provideServiceConfiguration(): AuthorizationServiceConfiguration {
        return AuthorizationServiceConfiguration(
            Uri.parse(BuildConfig.AUTH_END_POINT), // authorization endpoint
            Uri.parse(BuildConfig.TOKEN_END_POINT), // token endpoint
        )
    }

    @Provides
    @Singleton
    fun provideAuthState(
        serviceConfiguration: AuthorizationServiceConfiguration
    ): AuthState {
        return AuthState(serviceConfiguration)
    }

    @Provides
    @Singleton
    fun provideAuthorizationRequest(
        serviceConfiguration: AuthorizationServiceConfiguration,
    ): AuthorizationRequest {
        return AuthorizationRequest
            .Builder(
                serviceConfiguration,
                CLIENT_ID,
                ResponseTypeValues.CODE,
                Uri.parse(BuildConfig.URL_REDIRECTION)
            ) // With this parameter we are sure to present login page
            .setPrompt("login")
            .setScope("openid offline_access")
            .build()

    }

    @Provides
    @Singleton
    fun provideAuthService(
        @ApplicationContext context: Context
    ): AuthorizationService {
        return AuthorizationService(context)
    }


    @Provides
    fun provideAuthIntent(
        authorizationService: AuthorizationService,
        authorizationRequest: AuthorizationRequest
    ): Intent {
        return authorizationService.getAuthorizationRequestIntent(authorizationRequest)
    }
}