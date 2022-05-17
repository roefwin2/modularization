package com.ellcie.ellcieauthenticationapp.usecases.firebasedatausecase

import com.ellcie.ellcieauthenticationapp.network.repositories.BackOfficeRepositoryImpl
import com.ellcie.ellcieauthenticationapp.network.repositories.FirebaseRepositoryImpl
import com.ellcie.toolkitlibrary.resource.Resource
import com.google.firebase.database.DataSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetDefaultProfileUseCase @Inject constructor(private val firebaseRepositoryImpl: FirebaseRepositoryImpl) {

    suspend fun invoked() : Flow<Resource<String>> {
        return firebaseRepositoryImpl.getDefaultProfile().map {
            it
        }
    }
}