package com.ellcie.ellcieauthenticationapp.usecases

import android.content.Intent
import com.ellcie.ellcieauthenticationapp.network.repositories.AuthenticationRepositoryImpl
import com.ellcie.toolkitlibrary.resource.Resource
import com.ellcie.toolkitlibrary.userauth.UserAuth
import kotlinx.coroutines.flow.channelFlow
import javax.inject.Inject

/**
 * Use the authentication repository in our app module
 * @param authenticationRepositoryImpl inject the EllcieAuth Manager from the authentication library
 */
class AuthorizationUseCase @Inject constructor(private val authenticationRepositoryImpl: AuthenticationRepositoryImpl) {
  //TODO remove collect not safe collect always
    suspend fun invoke(data: Intent?) = channelFlow<Resource<UserAuth>> {
       authenticationRepositoryImpl.keycloakLogin(data).collect {
           send(it)
       }
    }
}