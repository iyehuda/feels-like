package com.iyehuda.feelslike.ui.login

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.iyehuda.feelslike.R
import com.iyehuda.feelslike.data.AuthRepository
import com.iyehuda.feelslike.data.utils.Result
import kotlinx.coroutines.launch

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {
    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    val loginResult: LiveData<LoginResult> = authRepository.userLogin.map { result ->
        when (result) {
            is Result.Success -> {
                LoginResult(success = result.data)
            }

            is Result.Error -> {
                LoginResult(error = result.exception.errorStringRes)
            }

            null -> {
                LoginResult()
            }
        }
    }

    fun login(
        email: String,
        password: String,
    ) = viewModelScope.launch {
        authRepository.login(email, password)
    }

    fun loginDataChanged(email: String, password: String) {
        val emailError = validateEmail(email)
        val passwordError = validatePassword(password)
        val empty = email.isEmpty() && password.isEmpty()
        val error = emailError != null || passwordError != null

        _loginForm.value = when {
            empty -> LoginFormState(isDataValid = false)
            error -> LoginFormState(
                isDataValid = false, emailError = emailError, passwordError = passwordError
            )

            else -> LoginFormState(isDataValid = true)
        }
    }

    private fun validateEmail(email: String): Int? =
        if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) null else R.string.invalid_email

    private fun validatePassword(password: String): Int? =
        if (password.length > 5) null else R.string.invalid_password
}
