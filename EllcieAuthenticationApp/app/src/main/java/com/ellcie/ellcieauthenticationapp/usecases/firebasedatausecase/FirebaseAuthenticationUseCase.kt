package com.ellcie.ellcieauthenticationapp.usecases.firebasedatausecase

import com.ellcie.ellcieauthenticationapp.network.repositories.FirebaseRepositoryImpl
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class FirebaseAuthenticationUseCase @Inject constructor(private val firebaseRepositoryImpl: FirebaseRepositoryImpl) {

    suspend fun invoke(custom: String) = flow {
        firebaseRepositoryImpl.firebaseSignIn(custom).collect{
            emit(it)
        }
    }
}