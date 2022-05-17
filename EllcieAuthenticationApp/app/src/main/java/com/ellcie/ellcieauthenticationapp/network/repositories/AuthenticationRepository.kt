package com.ellcie.ellcieauthenticationapp.network.repositories

import android.content.Intent
import com.ellcie.ellcieauthenticationlibrary.authmanager.EllcieAuthManagerImpl
import com.ellcie.toolkitlibrary.resource.Resource
import com.ellcie.toolkitlibrary.userauth.UserAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Interface for the Open/Close principle
 */
interface AuthenticationRepository

/**
 * Implementation of the interface for the authentication
 * @param ellcieAuthManagerImpl inject the EllcieAuth Manager from the authentication library
 */
class AuthenticationRepositoryImpl @Inject constructor(private val ellcieAuthManagerImpl: EllcieAuthManagerImpl) :
    AuthenticationRepository {
    //TODO remove collect not safe collect always
    /**
     * Function from the ellcieAuthManager for the keycloakLogin
     */
    suspend fun keycloakLogin(data: Intent?) = channelFlow {
        try {
            ellcieAuthManagerImpl.login(data).collect {
                send(it)
            }
        } catch (e: Exception) {
            send(Resource.Error(e.toString()))
        }
    }

    /**
     * Trigger the logout function from the authentication library
     */
    suspend fun logout(): Flow<Resource<UserAuth>> {
        return ellcieAuthManagerImpl.logout().map {
            it
        }
    }
}