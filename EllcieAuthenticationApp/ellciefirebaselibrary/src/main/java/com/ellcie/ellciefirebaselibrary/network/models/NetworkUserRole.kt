package com.ellcie.ellciefirebaselibrary.network.models

import android.util.Log
import com.ellcie.ellciefirebaselibrary.utils.FirebaseConstants.TAG
import com.squareup.moshi.Json

data class NetworkUserRole(
    @field:Json(name = "role") val role: String,
) {
    companion object {

        fun HashMap<String, String>.toNetworkUserRole(): NetworkUserRole {
            try {
                val role: String = get("role").toString()
                return NetworkUserRole(role)
            } catch (e: Exception) {
                Log.e(TAG, "Error converting user role ", e)
                return NetworkUserRole("error role")
            }
        }
    }
}
