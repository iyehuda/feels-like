package com.iyehuda.feelslike.ui.login

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.iyehuda.feelslike.R
import com.iyehuda.feelslike.data.AuthRepository
import com.iyehuda.feelslike.data.utils.Result
import com.iyehuda.feelslike.ui.base.BaseViewModel

class LoginViewModel(private val authRepository: AuthRepository) : BaseViewModel() {
    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    fun login(
        email: String,
        password: String,
        callback: (LoginResult) -> Unit,
    ) = safeLaunch {
        val loginResult = when (val result = authRepository.login(email, password)) {
            is Result.Success -> {
                LoginResult(success = result.data)
            }

            is Result.Error -> {
                LoginResult(error = result.exception.errorStringRes)
            }

            else -> {
                LoginResult()
            }
        }

        callback(loginResult)
    }

    fun loginDataChanged(email: String, password: String) {
        val emailError = validateEmail(email)
        val passwordError = validatePassword(password)
        val empty = setOf(email, password).any { it.isEmpty() }
        val error = setOf(emailError, passwordError).any { it != null } || empty

        _loginForm.value = when {
            error -> LoginFormState(
                isDataValid = false, emailError = emailError, passwordError = passwordError
            )

            else -> LoginFormState(isDataValid = true)
        }
    }

    private fun validateEmail(email: String): Int? =
        if (email.isEmpty() || Patterns.EMAIL_ADDRESS.matcher(email)
                .matches()
        ) null else R.string.invalid_email

    private fun validatePassword(password: String): Int? =
        if (password.isEmpty() || password.length >= 6) null else R.string.invalid_password
}
