package com.ellcie.ellcieauthenticationapp.ui.sos

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
import androidx.navigation.fragment.findNavController
import com.ellcie.ellcieauthenticationapp.R
import com.ellcie.ellcieauthenticationapp.databinding.SosFragmentBinding
import com.ellcie.toolkitlibrary.actionstate.ActionState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SosFragment : Fragment() {

    private var _binding: SosFragmentBinding? = null
    private val binding : SosFragmentBinding get() = _binding!!

    private val viewModel: SosViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launchWhenResumed {
            viewModel.sosSate.collect {
                processSosScreen(it)
            }
        }
        viewModel.engageSos()
    }

    private fun processSosScreen(sosScreen: SosScreen) {
        binding.loading.isVisible = false
        when (sosScreen.engageSos) {
            ActionState.NOT_STARTED -> {
            }
            ActionState.PENDING -> {
                binding.loading.isVisible = true
            }
            ActionState.SUCCESS -> {
                binding.cancelSosBtn.apply {
                    text = "STOP"
                    background = requireContext().getDrawable(R.drawable.ic_launcher_background)
                    setOnClickListener {
                        viewModel.cancelSos()
                    }
                }
            }
            ActionState.STOP -> findNavController().popBackStack()
            ActionState.ERROR -> {
                Toast.makeText(
                    requireContext(),
                    "Error stop sos",
                    Toast.LENGTH_LONG
                ).show()
                findNavController().popBackStack()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SosFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

}