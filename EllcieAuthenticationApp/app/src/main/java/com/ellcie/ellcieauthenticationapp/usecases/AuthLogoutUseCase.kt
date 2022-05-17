package com.ellcie.ellcieauthenticationapp.usecases

import com.ellcie.ellcieauthenticationapp.network.repositories.AuthenticationRepositoryImpl
import com.ellcie.toolkitlibrary.resource.Resource
import com.ellcie.toolkitlibrary.userauth.UserAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * logout use case
 * @param authenticationRepositoryImpl inject the ellcieAutManager from the authentication library
 * @return flow of resource UserAuth
 */
class AuthLogoutUseCase @Inject constructor(private val authenticationRepositoryImpl: AuthenticationRepositoryImpl){

    suspend fun invoke() : Flow<Resource<UserAuth>> {
        return authenticationRepositoryImpl.logout().map {
            it
        }
    }
}