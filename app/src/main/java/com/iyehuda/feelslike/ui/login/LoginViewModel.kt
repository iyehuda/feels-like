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
        if (!isEmailNameValid(email)) {
            _loginForm.value = LoginFormState(emailError = R.string.invalid_email)
        } else if (!isPasswordValid(password)) {
            _loginForm.value = LoginFormState(passwordError = R.string.invalid_password)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }

    private fun isEmailNameValid(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }
}
