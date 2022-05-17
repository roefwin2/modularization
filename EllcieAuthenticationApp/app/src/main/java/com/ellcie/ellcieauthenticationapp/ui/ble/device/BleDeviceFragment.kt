package com.ellcie.ellcieauthenticationapp.ui.ble.device

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.ellcie.ellcieauthenticationapp.R
import com.ellcie.ellcieauthenticationapp.databinding.BleDeviceFragmentBinding
import com.ellcie.ellcieauthenticationapp.databinding.BleScanFragmentBinding
import com.ellcie.toolkitlibrary.resource.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BleDeviceFragment : Fragment() {

    private var _binding: BleDeviceFragmentBinding? = null
    private val binding: BleDeviceFragmentBinding get() = _binding!!

    private val viewModel: BleDeviceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launchWhenResumed {
            viewModel.deviceState.collect {
                processDeviceScreen(it)
            }
        }
        viewModel.getBatteryLevel()
        viewModel.getChargingState()

    }

    private fun processDeviceScreen(deviceScreen: DeviceScreen) {
        binding.loading.isVisible = false
        when(val battery = deviceScreen.batteryLevel){
            is Resource.Error -> Toast.makeText(requireContext(),battery.cause,Toast.LENGTH_LONG).show()
            Resource.Loading -> {
                binding.loading.isVisible = true
            }
            is Resource.Success -> {
                binding.loading.isVisible = false
                binding.batteryIndicatror.chargeLevel = battery.value
            }
        }
        when(val isCharging = deviceScreen.isCharging){
            is Resource.Success -> {
                binding.loading.isVisible = false
                binding.batteryIndicatror.isCharging = isCharging.value
            }
            else -> Toast.makeText(
                requireContext(),
                "Error isCharging observable",
                Toast.LENGTH_LONG
            ).show()
        }
        when (val localizeState = deviceScreen.localizeMeState) {
            is Resource.Error -> Toast.makeText(
                requireContext(),
                localizeState.cause,
                Toast.LENGTH_LONG
            ).show()
            Resource.Loading -> {
                binding.loading.isVisible = true
            }
            is Resource.Success -> {
                binding.loading.isVisible = false
                Toast.makeText(
                    requireContext(),
                    localizeState.value,
                    Toast.LENGTH_LONG
                ).show()
                binding.localizeBtn.apply {
                    text = "SOS"

                    setOnClickListener {
                        val action = R.id.action_bleDeviceFragment_to_graphFragment
                        findNavController().navigate(action)
                    }
                }
            }
        }

        when (val disconnect = deviceScreen.disconnectState) {
            is Resource.Error -> Toast.makeText(
                requireContext(),
                disconnect.cause,
                Toast.LENGTH_LONG
            ).show()
            Resource.Loading -> {
                binding.loading.isVisible = true
            }
            is Resource.Success -> {
                binding.loading.isVisible = false
                Toast.makeText(requireContext(), disconnect.value, Toast.LENGTH_LONG).show()
                findNavController().popBackStack()
            }

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BleDeviceFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.localizeBtn.setOnClickListener {
            viewModel.localizeMe()
        }
        binding.disconnectBtn.setOnClickListener {
            viewModel.disconnectToDevice()
        }
    }

}