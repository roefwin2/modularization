package com.ellcie.ellcieauthenticationlibrary.injection

import android.content.ContentValues
import android.util.Log
import com.ellcie.ellcieauthenticationlibrary.BuildConfig
import com.ellcie.ellcieauthenticationlibrary.network.api.AuthApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

/**
 * Injection of the Network utils for the authentication library in the dependency tree
 */
@Module
@InstallIn(SingletonComponent::class)
//may create un NetworkModule factory in toolkit duplicate code in auth and backoffice library
internal class AuthNetworkModule {
    /**
     * Interface API implementation
     */
    @Provides
    fun provideRetrofitAuthApi(
        okHttpClient: OkHttpClient,
        moshi: Moshi
    ): AuthApi {

        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_END_POINT)
            .client(okHttpClient)
            // Add converter to JSONify objects with Moshi
            .addConverterFactory(MoshiConverterFactory.create(moshi).failOnUnknown())
            .build()
        return retrofit.create(AuthApi::class.java)
    }

    /**
     * OkHttpClient for the requests
     */
    @Provides
    fun provideOkHttpClient(
    ): OkHttpClient {
        val interceptor = HttpLoggingInterceptor {
            // TODO: improve error logging to not report sensitive data like passwords
            Log.d(ContentValues.TAG, it)
        }
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        return OkHttpClient.Builder()
            //Add only clear text because we connect to the EV simulator in Http
            .addInterceptor(interceptor)
            .build()
    }

    /**
     * parser for the json
     */
    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        // See https://github.com/square/moshi#kotlin
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

}