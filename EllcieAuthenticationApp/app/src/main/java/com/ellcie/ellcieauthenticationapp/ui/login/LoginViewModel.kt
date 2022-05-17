package com.ellcie.ellcieauthenticationapp.ui.login

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ellcie.ellcieauthenticationapp.usecases.AuthLogoutUseCase
import com.ellcie.ellcieauthenticationapp.usecases.CheckNewCguUseCase
import com.ellcie.ellcieauthenticationapp.usecases.TotalAuthenticationUseCase
import com.ellcie.ellcieauthenticationapp.usecases.firebasedatausecase.GetDefaultProfileUseCase
import com.ellcie.ellcieauthenticationapp.usecases.sharedpreferenceusecase.GetSharedPreferenceIsLogged
import com.ellcie.ellcieauthenticationapp.usecases.sharedpreferenceusecase.SaveInSharedPreferenceIsLogged
import com.ellcie.ellcieauthenticationlibrary.authmanager.EllcieAuthManagerImpl
import com.ellcie.ellciefirebaselibrary.firebasemanager.EllcieFirebaseDataManager
import com.ellcie.toolkitlibrary.resource.Resource
import com.ellcie.toolkitlibrary.userauth.UserAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Use in the viewModel all the usecases to trigger action
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    val ellcieAuthManagerImpl: EllcieAuthManagerImpl,
    private val totalAuthenticationUseCase: TotalAuthenticationUseCase,
    private val authLogoutUseCase: AuthLogoutUseCase,
    private val checkNewCguUseCase: CheckNewCguUseCase,
    private val saveInSharedPreferenceIsLogged: SaveInSharedPreferenceIsLogged,
    private val getDefaultProfileUseCase: GetDefaultProfileUseCase,
    getSharedPreferenceIsLogged: GetSharedPreferenceIsLogged,
    private val ellcieFirebaseDataManager: EllcieFirebaseDataManager
) :
    ViewModel() {
    /**
     * Mutable state to get the actions response only in the viewmodel
     */
    private val _loginResult = MutableLiveData<Resource<UserAuth>>()

    /**
     * Immutable state listened by the UI
     */
    val loginResult: LiveData<Resource<UserAuth>> get() = _loginResult

    private val _dataResult = MutableLiveData<Resource<String>>()
    val dataResult: LiveData<Resource<String>> get() = _dataResult

    /**
     * recreate the former logged session after recreate VM with SharedPref
     */
    //TODO May be use in the future saveStateHandle


    /**
     * Login action trigger by the user
     */
    fun login(data: Intent?) {
        // can be launched in a separate asynchronous job
        viewModelScope.launch {
            totalAuthenticationUseCase.invoke(data).collect {
                if (it is Resource.Success) {
                    saveInSharedPreferenceIsLogged.invoke(true)
                } else {
                    saveInSharedPreferenceIsLogged.invoke(false)
                }
                _loginResult.postValue(it)
            }
        }
    }

    /**
     * Logout action trigger by the user
     */
    fun logout() {
        viewModelScope.launch {
            authLogoutUseCase.invoke().collect {
                if (it is Resource.Success) {
                    saveInSharedPreferenceIsLogged.invoke(false)
                } else {
                    saveInSharedPreferenceIsLogged.invoke(true)
                }
                _loginResult.postValue(it)
            }
        }
    }


    /**
     * Get the cgu data trigger by the user
     */
    fun checkCgu() {
        viewModelScope.launch {
            checkNewCguUseCase.invoked().collect {
                _dataResult.postValue(it)
            }
        }
    }
}
