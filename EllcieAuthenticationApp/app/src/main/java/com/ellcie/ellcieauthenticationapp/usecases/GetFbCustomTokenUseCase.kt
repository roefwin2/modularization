package com.ellcie.ellcieauthenticationapp.usecases

import com.ellcie.ellcieauthenticationapp.network.repositories.BackOfficeRepositoryImpl
import com.ellcie.toolkitlibrary.resource.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetFbCustomTokenUseCase @Inject constructor(
    private val backOfficeRepositoryImpl: BackOfficeRepositoryImpl,
) {

    suspend fun invoke(accessToken: String): Flow<Resource<String>> {
        backOfficeRepositoryImpl.initBackOffice(accessToken)
        return backOfficeRepositoryImpl.getFbCustomToken().map {
            it
        }
    }
}