package com.ellcie.ellcieauthenticationlibrary.network.api

import com.ellcie.ellcieauthenticationlibrary.utils.Constants
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/**
 * Interface for the logout call
 */
interface AuthApi {
    @FormUrlEncoded
    @POST(Constants.URL_LOGOUT)
    suspend fun postLogout(
        @Field("client_id") clientId: String,
        @Field("refresh_token") refreshToken: String
    ): Response<Unit>
}