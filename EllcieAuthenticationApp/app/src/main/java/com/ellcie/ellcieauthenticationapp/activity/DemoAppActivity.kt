package com.ellcie.ellcieauthenticationapp.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import com.ellcie.ellcieauthenticationapp.R
import com.ellcie.ellcieauthenticationapp.databinding.ActivityLoginBinding
import com.ellcie.ellcieauthenticationapp.ui.ble.device.BleDeviceFragment
import com.ellcie.ellcieauthenticationapp.ui.ble.scan.BleScanFragment
import com.ellcie.nordicblelibrary.services.EhBleForegroundActivityReadyImpl
import com.ellcie.toolkitlibrary.resource.Resource
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


//TODO not extend main activity with the foreground service + must inject in module because not extend directly AppCompat
@AndroidEntryPoint
class DemoAppActivity @Inject constructor() : EhBleForegroundActivityReadyImpl() {
    private var _binding: ActivityLoginBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: DemoAppModuleSharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root

        setContentView(view)
        super.onCreateView(savedInstanceState)
        setSupportActionBar(binding.myToolbar)
        lifecycleScope.launchWhenResumed {
            sharedViewModel.sharedState.collect {
                processSharedDataScreen(it)
            }
        }
        sharedViewModel.startUserSession()
    }

    private fun processSharedDataScreen(state: SharedDataSessionState) {
        when (state) {
            is SharedDataSessionState.ConnectedUser -> {
                when (val userRole = state.userRole) {
                    is Resource.Success -> {
                        binding.myToolbar?.title = userRole.value
                        when (val deviceConnection = state.bleState) {
                            is Resource.Error -> {
                            }
                            Resource.Loading -> {
                            }
                            is Resource.Success -> {
                                val navController =
                                    findNavController(R.id.nav_host_fragment_container)
                                if (deviceConnection.value is BleState.BleEnable) {
                                    binding.myToolbar?.subtitle = (deviceConnection.value as BleState.BleEnable).deviceConnected
                                    navController.navigate(R.id.bleDeviceFragment)
                                } else {
                                    navController.navigate(R.id.bleScanFragment)
                                }
                            }
                        }
                    }
                    is Resource.Error -> binding.myToolbar?.title = userRole.cause
                    else -> {
                    }
                }
            }
            SharedDataSessionState.DisconnectedUser -> {
            }
            is SharedDataSessionState.ErrorState -> {
            }
            SharedDataSessionState.Loading -> {
            }
        }
    }


}
