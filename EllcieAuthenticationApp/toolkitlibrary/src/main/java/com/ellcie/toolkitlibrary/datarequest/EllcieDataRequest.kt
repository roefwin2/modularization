package com.ellcie.toolkitlibrary.datarequest

import com.ellcie.toolkitlibrary.resource.Resource
import com.ellcie.toolkitlibrary.userauth.UserAuth
import kotlinx.coroutines.flow.Flow

interface EllcieDataRequest {
    suspend fun signIn(customToken: String) : Flow<Resource<UserAuth>>
    fun getFirebaseAuthState() : Flow<String?>
}