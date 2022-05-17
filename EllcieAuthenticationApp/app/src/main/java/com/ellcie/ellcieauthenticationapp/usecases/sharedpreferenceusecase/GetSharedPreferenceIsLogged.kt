package com.ellcie.ellcieauthenticationapp.usecases.sharedpreferenceusecase

import android.content.SharedPreferences
import com.ellcie.ellcieauthenticationapp.usecases.sharedpreferenceusecase.authSate.GetSharedPreferenceAuthStateUsecase
import com.ellcie.ellcieauthenticationapp.utils.Constants.IS_CONNECTED
import net.openid.appauth.AuthState
import javax.inject.Inject

/**
 * Use case for get the connected status of the user
 */
class GetSharedPreferenceIsLogged @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val getSharedPreferenceAuthStateUsecase: GetSharedPreferenceAuthStateUsecase
) {

    fun invoke(): Pair<Boolean, AuthState?> {
        return Pair(
            sharedPreferences.getBoolean(IS_CONNECTED, false),
            getSharedPreferenceAuthStateUsecase.invoke()
        )
    }
}