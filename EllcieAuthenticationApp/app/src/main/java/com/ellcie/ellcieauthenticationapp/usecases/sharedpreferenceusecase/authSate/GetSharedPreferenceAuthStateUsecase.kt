package com.ellcie.ellcieauthenticationapp.usecases.sharedpreferenceusecase.authSate

import android.content.SharedPreferences
import com.ellcie.ellcieauthenticationapp.utils.Constants.AUTH_STATE
import net.openid.appauth.AuthState
import javax.inject.Inject

/**
 * Use case for get the last connected AutState user
 */
class GetSharedPreferenceAuthStateUsecase @Inject constructor(private val sharedPreferences: SharedPreferences) {

    fun invoke(): AuthState? {
        val json = sharedPreferences.getString(AUTH_STATE, null)
        return json?.let { AuthState.jsonDeserialize(it) }
    }
}