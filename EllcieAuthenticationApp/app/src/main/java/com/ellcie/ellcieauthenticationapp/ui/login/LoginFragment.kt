package com.ellcie.ellcieauthenticationapp.ui.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.ellcie.ellcieauthenticationapp.R
import com.ellcie.ellcieauthenticationapp.databinding.FragmentLoginBinding
import com.ellcie.toolkitlibrary.resource.Resource
import com.ellcie.toolkitlibrary.userauth.UserAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {
    private val PERMISSION_REQUEST_FINE_LOCATION: Int = 11
    private val REQUEST_ENABLE_BT: Int = 11
    private val PERMISSION_REQUEST_COARSE_LOCATION: Int = 10
    private val loginViewModel: LoginViewModel by viewModels()
    private lateinit var binding: FragmentLoginBinding

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                if (data != null) {
                    loginViewModel.login(data)
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(layoutInflater)
        /**
         * state for the User Auth state (authorize, authenticated etc ..) listened by the UI and change in the viewModel
         */
        loginViewModel.loginResult.observe(viewLifecycleOwner, {
            processUserAuthState(it, binding.logout, binding.loading, binding.login)
        })

        /**
         * state for the get cgu datas state (authorize, authenticated etc ..) listened by the UI and change in the viewModel
         */
        loginViewModel.dataResult.observe(viewLifecycleOwner, {
            processCguDataState(it, binding.loading)
        })
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.login.setOnClickListener {
            goToKeycloak()
        }
        binding.logout.setOnClickListener {
            loginViewModel.logout()
        }
    }

    private fun processCguDataState(
        it: Resource<String>?,
        loading: ProgressBar
    ) {
        val dataResult = it ?: return
        when (it) {
            is Resource.Error -> Toast.makeText(requireContext(), it.cause, Toast.LENGTH_LONG)
                .show()
            Resource.Loading -> {
                loading.isVisible = true
            }
            is Resource.Success -> {
                loading.isVisible = false
                Toast.makeText(
                    requireContext(),
                    it.value,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun processUserAuthState(
        it: Resource<UserAuth>?,
        logout: Button?,
        loading: ProgressBar,
        login: Button
    ) {
        val loginResult = it ?: return
        logout?.isVisible = false
        loading.isVisible = false
        when (it) {
            is Resource.Error -> Toast.makeText(requireContext(), it.cause, Toast.LENGTH_LONG)
                .show()
            Resource.Loading -> {
                loading.isVisible = true

            }
            is Resource.Success -> {
                loading.isVisible = false
                logout?.isVisible = true
                login.text = "Data from Back office"
                login.setOnClickListener {
                    loginViewModel.checkCgu()
                }
                //TODO combine in usecase login + init backoffice
                Toast.makeText(
                    requireContext(), when (val userAuth = it.value) {
                        is UserAuth.Authorize -> {
                            userAuth.accessToken
                        }
                        is UserAuth.UnAuthorize -> {
                            login.text = "SIGN OR REGISTER"
                            login.setOnClickListener {
                                goToKeycloak()
                            }
                            logout?.isVisible = false
                            userAuth.msg
                        }
                        is UserAuth.Authenticated -> userAuth.credential
                        else -> it.value.toString()
                    }, Toast.LENGTH_LONG
                ).show()

            }
        }
    }

    /**
     * launch the intent for the keycloack url action
     */
    private fun goToKeycloak() {
        try {
            resultLauncher.launch(loginViewModel.ellcieAuthManagerImpl.authIntent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), e.toString(), Toast.LENGTH_LONG).show()
        }
    }
}