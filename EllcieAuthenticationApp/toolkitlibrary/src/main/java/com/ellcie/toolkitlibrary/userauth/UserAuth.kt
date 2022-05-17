package com.ellcie.toolkitlibrary.userauth

import com.ellcie.toolkitlibrary.datarequest.EllcieDataRequest

sealed class UserAuth {

    data class Authorize(
        val accessToken: String,
    ) : UserAuth()

    data class UnAuthorize(
        val msg: String
    ) : UserAuth()

    data class Authenticated(
        val id: String,
        val credential: String,
        val ellcieDataRequest: EllcieDataRequest?
    ) : UserAuth()

    data class Unauthenticated(
        val msg: String
    ) : UserAuth()
}