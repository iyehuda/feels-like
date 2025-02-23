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
import androidx.navigation.fragment.findNavController
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.logEvent
import com.google.firebase.ktx.Firebase
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

        loginViewModel.loginFormState.observe(viewLifecycleOwner) {
            onLoginFormUpdated(it)
        }

        loginViewModel.loginResult.observe(viewLifecycleOwner) {
            it?.let {
                onLoginResult(it)
            }
        }

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
            Firebase.analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT) {
                param(FirebaseAnalytics.Param.ITEM_ID, "login_button")
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "button")
                param(FirebaseAnalytics.Param.SCREEN_NAME, "login")
            }
            performLogin()
        }
    }

    private fun onLoginFormUpdated(loginFormState: LoginFormState) {
        binding.loginButton.isEnabled = loginFormState.isDataValid

        if (loginFormState.emailError != null) {
            binding.emailEditText.error = getString(loginFormState.emailError)
        } else {
            loginFormState.passwordError?.let {
                binding.passwordEditText.error = getString(it)
            }
        }
    }

    private fun performLogin() {
        binding.loadingProgressBar.visibility = View.VISIBLE
        loginViewModel.login(
            binding.emailEditText.text.toString(), binding.passwordEditText.text.toString()
        )
    }

    private fun onLoginResult(loginResult: LoginResult) {
        Firebase.analytics.logEvent(FirebaseAnalytics.Event.LOGIN) {
            param(FirebaseAnalytics.Param.METHOD, "email")
            param(FirebaseAnalytics.Param.SUCCESS, (loginResult.success != null).toString())
            param(FirebaseAnalytics.Param.SCREEN_NAME, "login")

            loginResult.error?.let {
                param("error", getString(it))
            }
        }

        binding.loadingProgressBar.visibility = View.GONE
        loginResult.error?.let {
            onLoginFailed(it)
        }
        loginResult.success?.let {
            onLoginSucceeded(it)
        }
    }

    private fun onLoginSucceeded(model: UserDetails) {
        displayToast(getString(R.string.welcome, model.displayName))
        findNavController().navigate(R.id.action_successful_login)
    }

    private fun onLoginFailed(@StringRes errorString: Int) = displayToast(getString(errorString))

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
