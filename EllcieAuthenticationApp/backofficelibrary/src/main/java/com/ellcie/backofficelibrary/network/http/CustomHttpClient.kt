package com.ellcie.backofficelibrary.network.http

import android.content.ContentValues
import android.util.Log
import com.ellcie.backofficelibrary.interceptor.TokenInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import javax.inject.Inject

class CustomHttpClient @Inject constructor(private val tokenInterceptor: TokenInterceptor)  : OkHttpClient() {
    private val interceptor = HttpLoggingInterceptor {
        // TODO: improve error logging to not report sensitive data like passwords
        Log.d(ContentValues.TAG, it)
    }
    fun getCustomHttpClient(): OkHttpClient {
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        return OkHttpClient.Builder()
            //Add only clear text because we connect to the EV simulator in Http
            .addInterceptor(interceptor)
            .addInterceptor(tokenInterceptor)
            .build()
    }
}