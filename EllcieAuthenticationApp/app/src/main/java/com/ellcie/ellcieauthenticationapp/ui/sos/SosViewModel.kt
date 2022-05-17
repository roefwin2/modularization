package com.ellcie.ellcieauthenticationapp.ui.sos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ellcie.ellcieauthenticationapp.network.repositories.BleRepositoryImpl
import com.ellcie.toolkitlibrary.actionstate.ActionState
import com.ellcie.toolkitlibrary.resource.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SosViewModel@Inject constructor(private val bleRepositoryImpl: BleRepositoryImpl) : ViewModel() {
    private val _sosState : MutableStateFlow<SosScreen> = MutableStateFlow(SosScreen(ActionState.NOT_STARTED))
    val sosSate : StateFlow<SosScreen> get() = _sosState

    fun engageSos(){
        viewModelScope.launch {

        }
    }

    fun cancelSos(){
        viewModelScope.launch {

        }
    }
}

data class SosScreen(
    val engageSos : ActionState
)