package com.ellcie.ellcieauthenticationapp.network.repositories

import com.ellcie.backofficelibrary.backofficemanager.BackOfficeManagerImpl
import com.ellcie.ellcieauthenticationapp.usecases.RefreshTokenUseCase
import com.ellcie.toolkitlibrary.resource.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface BackOfficeRepository

/**
 * BORepository lifecycle link with application lifecycle
 * @param backOfficeManagerImpl BOManager to handle all the action for the BO
 * @param refreshTokenUseCase use case to trigger refresh token from other module of Oauth2 => use to OnCreate the BoManager and initialize it every time the BoRepo is recreated
 */

//TODO may be try to inject the refreshUsecase in the BOManager in DI but very complicated way (Multi-module)
class BackOfficeRepositoryImpl @Inject constructor(
    private val backOfficeManagerImpl: BackOfficeManagerImpl,
    refreshTokenUseCase: RefreshTokenUseCase
) : BackOfficeRepository {

    /**
     * Init the BoManager in the init of the repository to handle the SST principle
     *
     */
    init {
        backOfficeManagerImpl.onCreate {
            refreshTokenUseCase.invoke()
        }
    }

    /**
     * fun to call in the init step only once when we are authorize
     * */
    fun initBackOffice(defaultAccessToken: String) {
        backOfficeManagerImpl.tokenInterceptor.accessToken = defaultAccessToken
    }

    suspend fun getFbCustomToken(): Flow<Resource<String>> {
        return try {
            backOfficeManagerImpl.getFbCustomToken().map {
                it
            }
        } catch (e: Exception) {
            flowOf(Resource.Error(e.toString()))
        }
    }

    suspend fun getCheckNewCgu(): Flow<Resource<String>> {
        return try {
            backOfficeManagerImpl.getCgu().map {
                it
            }
        } catch (e: Exception) {
            flowOf(Resource.Error(e.toString()))
        }
    }
}