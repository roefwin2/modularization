package com.ellcie.ellcieauthenticationapp.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ellcie.ellcieauthenticationapp.network.repositories.BleRepositoryImpl
import com.ellcie.ellcieauthenticationapp.network.repositories.FirebaseRepositoryImpl
import com.ellcie.ellcieauthenticationapp.usecases.sharedpreferenceusecase.GetSharedPreferenceIsLogged
import com.ellcie.ellcieauthenticationlibrary.authmanager.EllcieAuthManagerImpl
import com.ellcie.toolkitlibrary.resource.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DemoAppModuleSharedViewModel @Inject constructor(
    private val firebaseRepositoryImpl: FirebaseRepositoryImpl,
    private val bleRepositoryImpl: BleRepositoryImpl,
    private val getSharedPreferenceIsLogged: GetSharedPreferenceIsLogged,
    private val ellcieAuthManagerImpl: EllcieAuthManagerImpl
) : ViewModel() {

    private val _sharedState: MutableStateFlow<SharedDataSessionState> =
        MutableStateFlow(SharedDataSessionState.DisconnectedUser)
    val sharedState: StateFlow<SharedDataSessionState> get() = _sharedState

    init {
        val isDeviceConnected = bleRepositoryImpl.bleDeviceConnection
        val getPref = getSharedPreferenceIsLogged.invoke()
        val initSharedVM =
            if (getPref.first) {
                ellcieAuthManagerImpl.restoreAuthState(getPref.second)
                SharedDataSessionState.ConnectedUser(
                    Resource.Loading,
                    Resource.Loading,
                    if (isDeviceConnected.first) Resource.Success(
                        BleState.BleEnable(
                            isDeviceConnected.second
                        )
                    ) else Resource.Success(BleState.BleDisable)
                )
            } else {
                SharedDataSessionState.DisconnectedUser
            }
        _sharedState.value = initSharedVM
    }


    fun startUserSession() {
        viewModelScope.launch {
            firebaseRepositoryImpl.getDefaultProfile().collect {
                val currentState = _sharedState.value
                if (currentState is SharedDataSessionState.ConnectedUser)
                    _sharedState.value = currentState.copy(
                        userRole =
                        when (it) {
                            is Resource.Error -> Resource.Error(it.cause)
                            Resource.Loading -> Resource.Loading
                            is Resource.Success -> Resource.Success(it.value)
                        }
                    )
            }
        }
    }
}

sealed class SharedDataSessionState() {
    data class ConnectedUser(
        val userName: Resource<String>,
        val userRole: Resource<String>,
        val bleState: Resource<BleState>
    ) : SharedDataSessionState()

    object Loading : SharedDataSessionState()
    object DisconnectedUser : SharedDataSessionState()
    data class ErrorState(val msg: String) : SharedDataSessionState()
}

sealed class BleState() {
    data class BleEnable(
        val deviceConnected: String?,
    ) : BleState()

    object BleDisable : BleState()
}