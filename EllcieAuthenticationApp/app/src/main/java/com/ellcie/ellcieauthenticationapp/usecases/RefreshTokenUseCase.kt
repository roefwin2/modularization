package com.ellcie.ellcieauthenticationapp.usecases

import com.ellcie.ellcieauthenticationlibrary.authmanager.EllcieAuthManagerImpl
import com.ellcie.toolkitlibrary.resource.Resource
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Refresh token to update the expired token when intercept 403
 * @param ellcieAuthManagerImpl inject the authManager
 */
class RefreshTokenUseCase @Inject constructor(private val ellcieAuthManagerImpl: EllcieAuthManagerImpl) {

    suspend fun invoke(): String {
        return (ellcieAuthManagerImpl.refreshToken()
            .first { it is Resource.Success } as Resource.Success).value
    }
}