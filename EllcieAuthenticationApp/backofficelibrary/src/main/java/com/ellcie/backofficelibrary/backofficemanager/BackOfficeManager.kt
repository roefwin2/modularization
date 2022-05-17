package com.ellcie.backofficelibrary.backofficemanager

import com.ellcie.backofficelibrary.interceptor.TokenInterceptor
import com.ellcie.backofficelibrary.network.api.BackOfficeApi
import com.ellcie.toolkitlibrary.resource.Resource
import kotlinx.coroutines.flow.flow
import javax.inject.Inject


class BackOfficeManagerImpl @Inject constructor() {

    @Inject
    internal lateinit var backOfficeRetrofit: BackOfficeApi
    @Inject
    lateinit var tokenInterceptor: TokenInterceptor


    /**
     * onCreate to initialize the BoManager to use interceptor with refresh suspend function
     */
    fun onCreate(refreshToken: (suspend () -> String)) {
        tokenInterceptor.refreshTokenListener = {
            refreshToken.invoke()
        }
    }

    suspend fun getFbCustomToken() = flow {
        emit(Resource.Loading)
        try {
            val result = backOfficeRetrofit.getCustomToken()
            emit(Resource.Success(result.token))
        } catch (e: Exception) {
            emit(Resource.Error(e.toString()))
        }
    }

    suspend fun getCgu() = flow {
        emit(Resource.Loading)
        try {
            val result = backOfficeRetrofit.checkNewCguDriver()
            emit(Resource.Success(result.string()))
        } catch (e: Exception) {
            emit(Resource.Error(e.toString()))
        }
    }

}