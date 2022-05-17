package com.ellcie.ellcieauthenticationapp.ui.ble.scan

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ellcie.ellcieauthenticationapp.R
import com.ellcie.ellcieauthenticationapp.databinding.BleScanFragmentBinding
import com.ellcie.ellcieauthenticationapp.ui.ble.ScannedDeviceAdapter
import com.ellcie.toolkitlibrary.resource.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BleScanFragment : Fragment() {

    private val PERMISSION_REQUEST_FINE_LOCATION: Int = 11
    private val REQUEST_ENABLE_BT: Int = 11
    private val PERMISSION_REQUEST_COARSE_LOCATION: Int = 10

    private val viewModel: BleScanViewModel by viewModels()
    private var _binding: BleScanFragmentBinding? = null
    private val binding get() = _binding!!

    lateinit var adapter: ScannedDeviceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = ScannedDeviceAdapter()
        lifecycleScope.launchWhenResumed {
            viewModel.bleScanState.collect {
                processBleScanScreen(it)
            }
        }
    }

    private fun processBleScanScreen(screen: BleScanScreen) {
        binding.button.isVisible = false
        adapter.data = screen.deviceList
        when (val state = screen.connectionState) {
            is Resource.Error -> Toast.makeText(requireContext(), state.cause, Toast.LENGTH_LONG)
                .show()
            Resource.Loading -> Toast.makeText(requireContext(), "Loading", Toast.LENGTH_LONG)
                .show()
            is Resource.Success -> {
                Toast.makeText(requireContext(), state.value, Toast.LENGTH_LONG).show()
                val direction = R.id.action_bleScanFragment_to_bleDeviceFragment
                findNavController().navigate(direction)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BleScanFragmentBinding.inflate(inflater)
        binding.scannedDeviceRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            this@apply.adapter = this@BleScanFragment.adapter
        }
        checkForFineLocationPermission()
        checkForLocationPermission()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            viewModel.startScan()
        }
    return binding.root
}

override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    adapter.listener = {
        viewModel.connectToDevice(it, requireActivity().javaClass)
    }
    binding.textView.setOnClickListener {
        viewModel.destroy()
    }
}

@RequiresApi(Build.VERSION_CODES.M)
private fun checkForLocationPermission() {
    // Make sure we have access coarse location enabled, if not, prompt the user to enable it
    if (activity?.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle("This app needs location access")
        builder.setMessage("Please grant location access so this app can detect  peripherals.")
        builder.setPositiveButton(android.R.string.ok, null)
        builder.setOnDismissListener {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                PERMISSION_REQUEST_COARSE_LOCATION
            )
        }
        builder.show()
    }
}

@RequiresApi(Build.VERSION_CODES.M)
private fun checkForFineLocationPermission() {
    // Make sure we have access coarse location enabled, if not, prompt the user to enable it
    if (activity?.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle("This app needs fine location access")
        builder.setMessage("Please grant fine location access so this app can detect  peripherals.")
        builder.setPositiveButton(android.R.string.ok, null)
        builder.setOnDismissListener {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_FINE_LOCATION
            )
        }
        builder.show()
    }
}

override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<String>, grantResults: IntArray
) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    when (requestCode) {
        PERMISSION_REQUEST_COARSE_LOCATION -> {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                println("coarse location permission granted")
            } else {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Functionality limited")
                builder.setMessage("Since location access has not been granted, this app will not be able to discover BLE beacons")
                builder.setPositiveButton(android.R.string.ok, null)
                builder.setOnDismissListener { }
                builder.show()
            }
            return
        }
        PERMISSION_REQUEST_FINE_LOCATION -> {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                println("coarse location permission granted")
            } else {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Functionality limited")
                builder.setMessage("Since fine location access has not been granted, this app will not be able to discover BLE beacons")
                builder.setPositiveButton(android.R.string.ok, null)
                builder.setOnDismissListener { }
                builder.show()
            }
            return
        }
    }
}

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.resetSate()
    }

}