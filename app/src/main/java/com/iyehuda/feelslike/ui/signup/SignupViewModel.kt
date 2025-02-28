package com.iyehuda.feelslike.ui.signup

import android.net.Uri
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.iyehuda.feelslike.R
import com.iyehuda.feelslike.data.AuthRepository
import com.iyehuda.feelslike.data.utils.Result
import com.iyehuda.feelslike.ui.base.BaseViewModel

class SignupViewModel(private val authRepository: AuthRepository) : BaseViewModel() {
    private val _signupForm = MutableLiveData<SignupFormState>()
    val signupFormState: LiveData<SignupFormState> = _signupForm
    private val _selectedImageUri = MutableLiveData(Uri.EMPTY)
    val selectedImageUri: LiveData<Uri> = _selectedImageUri

    fun signup(
        name: String,
        email: String,
        password: String,
        avatar: Uri,
        callback: (SignupResult) -> Unit,
    ) = safeLaunch {
        val signupResult =
            when (val result = authRepository.signup(name, email, password, avatar)) {
                is Result.Success -> {
                    SignupResult(success = result.data)
                }

                is Result.Error -> {
                    SignupResult(error = result.exception.errorStringRes)
                }

                else -> {
                    SignupResult()
                }
            }

        callback(signupResult)
    }

    fun onAvatarSelected(uri: Uri?) {
        _selectedImageUri.value = uri ?: Uri.EMPTY
    }

    fun signupDataChanged(displayName: String, email: String, password: String) {
        val displayNameError = validateDisplayName(displayName)
        val emailError = validateEmail(email)
        val passwordError = validatePassword(password)
        val empty = setOf(
            displayName, email, password, _selectedImageUri.value.toString()
        ).any { it.isEmpty() }
        val error = setOf(
            displayNameError, emailError, passwordError
        ).any { it != null } || empty

        _signupForm.value = when {
            error -> SignupFormState(
                isDataValid = false,
                displayNameError = displayNameError,
                emailError = emailError,
                passwordError = passwordError
            )

            else -> SignupFormState(isDataValid = true)
        }
    }

    private fun validateDisplayName(name: String): Int? =
        if (name.isEmpty() || name.length >= 2) null else R.string.invalid_display_name

    private fun validateEmail(email: String): Int? =
        if (email.isEmpty() || Patterns.EMAIL_ADDRESS.matcher(email)
                .matches()
        ) null else R.string.invalid_email

    private fun validatePassword(password: String): Int? =
        if (password.isEmpty() || password.length >= 6) null else R.string.invalid_password
}
