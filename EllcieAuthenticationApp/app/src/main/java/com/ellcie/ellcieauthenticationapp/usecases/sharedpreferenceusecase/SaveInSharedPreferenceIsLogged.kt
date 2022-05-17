package com.ellcie.ellcieauthenticationapp.usecases.sharedpreferenceusecase

import android.content.SharedPreferences
import androidx.core.content.edit
import com.ellcie.ellcieauthenticationapp.usecases.sharedpreferenceusecase.authSate.SaveInSharedPreferenceAutStateUseCase
import com.ellcie.ellcieauthenticationapp.utils.Constants.IS_CONNECTED
import javax.inject.Inject

/**
 * Usecase for keep the user data
 * @param sharedPreferences use the application sharedPref inject by DI
 * @param saveInSharedPreferenceAutStateUseCase use case for save also the authSate( VERY IMPORTANT : use for the refresh token)
 */
class SaveInSharedPreferenceIsLogged @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val saveInSharedPreferenceAutStateUseCase: SaveInSharedPreferenceAutStateUseCase
) {

    fun invoke(isLogged: Boolean) {
        sharedPreferences.edit {
            putBoolean(IS_CONNECTED, isLogged).commit()
        }
        saveInSharedPreferenceAutStateUseCase.invoke()
    }
}