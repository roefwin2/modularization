package com.ellcie.backofficelibrary.interceptor

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Interceptor to handle 403 and recreate th response with new refreshtoken
 * @param accessToken keep the accesstoken in the tokenIntercpetor
 * @param refreshTokenListener use to call suspend function to refresh token => add in the initalization of the BOManager
 */
class TokenInterceptor @Inject constructor() : Interceptor {
    //TODO May be to remove
    var accessToken: String = ""
        set(value) {
            field = value
        }
    var refreshTokenListener: (suspend () -> String)? = null

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val requestBuilder = original.newBuilder()
            .addHeader("Authorization", "Bearer $accessToken")
        val request = requestBuilder.build()
        var response = chain.proceed(request)
        return if (response.code == 403) {
            var newRequest = chain.request()
            //handle it as per ur need
            // Retrieve New Access Token from the authManager whatever...
            //TODO may be use more efficient coroutine builder
            runBlocking {
                withContext(Dispatchers.Main) {
                    val freshAccessToken = refreshTokenListener?.invoke()
                    if (freshAccessToken != null) {
                        //reset new accestoken
                        accessToken = freshAccessToken
                        response.close()
                        newRequest = response.request.newBuilder().removeHeader("Authorization")
                            .addHeader("Authorization", "Bearer $freshAccessToken").build()
                    }
                }
                response = chain.proceed(newRequest)
                response
            }
        } else
            response
    }
}