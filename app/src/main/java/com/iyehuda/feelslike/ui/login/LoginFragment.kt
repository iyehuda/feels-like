package com.iyehuda.feelslike.ui.login

import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.iyehuda.feelslike.R
import com.iyehuda.feelslike.data.model.UserDetails
import com.iyehuda.feelslike.databinding.FragmentLoginBinding
import com.iyehuda.feelslike.ui.ViewModelFactory

class LoginFragment : Fragment() {
    private val loginViewModel: LoginViewModel by viewModels { ViewModelFactory() }
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loginViewModel.loginFormState.observe(viewLifecycleOwner, Observer { loginFormState ->
            loginFormState ?: return@Observer

            binding.loginButton.isEnabled = loginFormState.isDataValid
            loginFormState.emailError?.let {
                binding.emailEditText.error = getString(it)
            }
            loginFormState.passwordError?.let {
                binding.passwordEditText.error = getString(it)
            }
        })

        loginViewModel.loginResult.observe(viewLifecycleOwner, Observer { loginResult ->
            loginResult ?: return@Observer

            binding.loadingProgressBar.visibility = View.GONE
            loginResult.error?.let {
                showLoginFailed(it)
            }
            loginResult.success?.let {
                updateUiWithUser(it)
            }
        })

        val afterTextChangedListener = { _: Editable? ->
            loginViewModel.loginDataChanged(
                binding.emailEditText.text.toString(), binding.passwordEditText.text.toString()
            )
        }
        binding.emailEditText.doAfterTextChanged(afterTextChangedListener)
        binding.passwordEditText.doAfterTextChanged(afterTextChangedListener)
        binding.passwordEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                performLogin()
            }
            false
        }
        binding.loginButton.setOnClickListener {
            performLogin()
        }
    }

    private fun performLogin() {
        binding.loadingProgressBar.visibility = View.VISIBLE
        loginViewModel.login(
            binding.emailEditText.text.toString(), binding.passwordEditText.text.toString()
        )
    }

    private fun updateUiWithUser(model: UserDetails) {
        displayToast(getString(R.string.welcome, model.displayName))
        findNavController().navigate(R.id.action_successful_login)
    }

    private fun showLoginFailed(@StringRes errorString: Int) = displayToast(getString(errorString))

    private fun displayToast(message: String) {
        context?.applicationContext?.let { appContext ->
            Toast.makeText(appContext, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
