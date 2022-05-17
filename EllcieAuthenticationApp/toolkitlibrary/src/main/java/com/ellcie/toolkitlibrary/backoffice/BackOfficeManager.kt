package com.ellcie.toolkitlibrary.backoffice

import com.ellcie.toolkitlibrary.resource.Resource
import kotlinx.coroutines.flow.Flow

interface BackOfficeManager {
    suspend fun getCgu() : Flow<Resource<String>>
    suspend fun getLastCgu() : Flow<Resource<String>>
    suspend fun getCustomToken() : Flow<Resource<String>>
}