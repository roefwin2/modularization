package com.ellcie.backofficelibrary.network.model

import com.squareup.moshi.Json

data class CustomToken(
    @Json(name = "token") val token : String
)

