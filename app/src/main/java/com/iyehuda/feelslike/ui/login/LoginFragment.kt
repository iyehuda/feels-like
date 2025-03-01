package com.iyehuda.feelslike.ui.login

import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.iyehuda.feelslike.R
import com.iyehuda.feelslike.data.model.UserDetails
import com.iyehuda.feelslike.data.utils.explainableErrorOrNull
import com.iyehuda.feelslike.databinding.FragmentLoginBinding
import com.iyehuda.feelslike.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : BaseFragment<FragmentLoginBinding>() {
    private val viewModel: LoginViewModel by viewModels()

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ) = FragmentLoginBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.formState.observe(viewLifecycleOwner) {
            onFormUpdated(it)
        }

        val afterTextChangedListener = { _: Editable? ->
            viewModel.updateFormData(
                binding.emailEditText.text.toString(), binding.passwordEditText.text.toString()
            )
        }
        binding.emailEditText.doAfterTextChanged(afterTextChangedListener)
        binding.passwordEditText.doAfterTextChanged(afterTextChangedListener)
        binding.loginButton.setOnClickListener {
            submit()
        }

        binding.signupLink.setOnClickListener {
            findNavController().navigate(R.id.action_go_to_signup)
        }
    }

    private fun onFormUpdated(loginFormState: LoginFormState) {
        binding.loginButton.isEnabled = loginFormState.isDataValid

        loginFormState.emailError?.let {
            binding.emailEditText.error = getString(it)
        }
        loginFormState.passwordError?.let {
            binding.passwordEditText.error = getString(it)
        }
    }

    private fun submit() {
        binding.loadingProgressBar.visibility = View.VISIBLE
        viewModel.login(
            binding.emailEditText.text.toString(), binding.passwordEditText.text.toString()
        ) {
            viewLifecycleOwner.lifecycleScope.launch {
                onSubmitResult(it)
            }
        }
    }

    private fun onSubmitResult(loginResult: Result<UserDetails>) {
        binding.loadingProgressBar.visibility = View.GONE
        loginResult.explainableErrorOrNull()?.let {
            displayToast(it)
        }
        loginResult.getOrNull()?.let {
            displayToast(getString(R.string.login_greeting, it.displayName))
            findNavController().navigate(R.id.action_sign_in)
        }
    }
}
