package com.ellcie.ellcieauthenticationapp.usecases.sharedpreferenceusecase.authSate

import android.content.SharedPreferences
import androidx.core.content.edit
import com.ellcie.ellcieauthenticationapp.utils.Constants.AUTH_STATE
import com.ellcie.ellcieauthenticationlibrary.authmanager.EllcieAuthManagerImpl
import javax.inject.Inject

/**
 * Use case for set the last connected AutState user
 * @param sharedPreferences application sharePref inject by DI
 * @param ellcieAuthManagerImpl EllcieAuthManager for get the current AuthState important FOR TRIGGER THE REFRESH FUNCTION when tokenInterceptor catch 403 and trigger th refreshtoken suspend function from authManager
 */
class SaveInSharedPreferenceAutStateUseCase @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val ellcieAuthManagerImpl: EllcieAuthManagerImpl
) {

    fun invoke() {
        sharedPreferences.edit {
            putString(AUTH_STATE, ellcieAuthManagerImpl.getAuthStateInJson()).commit()
        }
    }
}