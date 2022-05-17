package com.ellcie.backofficelibrary.injection

import com.ellcie.backofficelibrary.BuildConfig
import com.ellcie.backofficelibrary.interceptor.TokenInterceptor
import com.ellcie.backofficelibrary.network.api.BackOfficeApi
import com.ellcie.backofficelibrary.network.http.CustomHttpClient
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class BackOfficeNetworkModule {

    @Provides
    fun provideRetrofitBackOfficeApi(
        customHttpClient: CustomHttpClient,
        moshi: Moshi
    ): BackOfficeApi {

        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.API_END_POINT)
            .client(customHttpClient.getCustomHttpClient())
            // Add converter to JSONify objects with Moshi
            .addConverterFactory(MoshiConverterFactory.create(moshi).failOnUnknown())
            .build()
        return retrofit.create(BackOfficeApi::class.java)
    }


    @Provides
    fun provideOkHttpClient(
        tokenInterceptor: TokenInterceptor
    ): CustomHttpClient {
        return CustomHttpClient(tokenInterceptor)
    }


    @Provides
    @Singleton
    fun provideTokenInterceptor(): TokenInterceptor {
        //TODO use un model object to wrapaccestoken {NO_TOKEN,ACESS_TOKEN}
        return TokenInterceptor()
    }
}

