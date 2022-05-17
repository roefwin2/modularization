package com.ellcie.toolkitlibrary.userauth

import android.content.Intent
import com.ellcie.toolkitlibrary.resource.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Interface for the AuthManager
 */
interface EllcieAuthManager {
    val currentAuth: StateFlow<Resource<UserAuth>>
    suspend fun login(data: Intent?): Flow<Resource<UserAuth>>
    suspend fun logout() : Flow<Resource<UserAuth>>
    suspend fun refreshToken() : Flow<Resource<String>>
}