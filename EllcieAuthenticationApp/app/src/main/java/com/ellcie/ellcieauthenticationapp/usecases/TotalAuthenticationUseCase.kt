package com.ellcie.ellcieauthenticationapp.usecases

import android.content.Intent
import com.ellcie.ellcieauthenticationapp.usecases.firebasedatausecase.FirebaseAuthenticationUseCase
import com.ellcie.toolkitlibrary.resource.Resource
import com.ellcie.toolkitlibrary.userauth.UserAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class TotalAuthenticationUseCase @Inject constructor(
    private val authorizationUseCase: AuthorizationUseCase,
    private val initBackOfficeManagerUseCase: GetFbCustomTokenUseCase,
    private val firebaseAuthenticationUseCase: FirebaseAuthenticationUseCase
) {

    suspend fun invoke(data: Intent?): Flow<Resource<UserAuth>> {
        return authorizationUseCase.invoke(data).flatMapMerge { it ->
            when (it) {
                is Resource.Error -> flowOf(Resource.Error(it.cause))
                Resource.Loading -> flowOf(Resource.Loading)
                is Resource.Success -> {
                    val userAuth = it.value
                    if (userAuth is UserAuth.Authorize) {
                        initBackOfficeManagerUseCase.invoke(userAuth.accessToken)
                            .flatMapMerge { resource ->
                                when (resource) {
                                    is Resource.Error -> flowOf(Resource.Error(resource.cause))
                                    Resource.Loading -> flowOf(Resource.Loading)
                                    is Resource.Success -> firebaseAuthenticationUseCase.invoke(
                                        resource.value
                                    )
                                }
                            }
                    } else {
                        flowOf(Resource.Error("error userAuth not compatible => not authorize"))
                    }
                }

            }
        }
    }
}