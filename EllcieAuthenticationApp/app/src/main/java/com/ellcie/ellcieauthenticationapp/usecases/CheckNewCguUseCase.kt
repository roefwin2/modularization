package com.ellcie.ellcieauthenticationapp.usecases

import com.ellcie.ellcieauthenticationapp.network.repositories.BackOfficeRepositoryImpl
import com.ellcie.toolkitlibrary.resource.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CheckNewCguUseCase @Inject constructor(private val backOfficeRepositoryImpl: BackOfficeRepositoryImpl) {

    suspend fun invoked() : Flow<Resource<String>> {
        return backOfficeRepositoryImpl.getCheckNewCgu().map {
            it
        }
    }
}