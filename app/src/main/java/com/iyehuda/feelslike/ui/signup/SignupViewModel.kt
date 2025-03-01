package com.iyehuda.feelslike.ui.signup

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.iyehuda.feelslike.data.auth.AuthRepository
import com.iyehuda.feelslike.data.model.UserDetails
import com.iyehuda.feelslike.data.utils.FormValidator.Companion.validateDisplayName
import com.iyehuda.feelslike.data.utils.FormValidator.Companion.validateEmail
import com.iyehuda.feelslike.data.utils.FormValidator.Companion.validatePassword
import com.iyehuda.feelslike.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SignupViewModel @Inject constructor(private val authRepository: AuthRepository) :
    BaseViewModel() {
    private val _formState = MutableLiveData<SignupFormState>()
    val formState: LiveData<SignupFormState> = _formState
    private val _selectedAvatar = MutableLiveData(Uri.EMPTY)
    val selectedAvatar: LiveData<Uri> = _selectedAvatar

    fun signup(
        name: String,
        email: String,
        password: String,
        avatar: Uri,
        callback: (Result<UserDetails>) -> Unit,
    ) = safeLaunch {
        val result = authRepository.signup(name, email, password, avatar)
        callback(result)
    }

    fun updateAvatar(uri: Uri?) {
        _selectedAvatar.value = uri ?: Uri.EMPTY
    }

    fun updateFormData(displayName: String, email: String, password: String) {
        val displayNameError = validateDisplayName(displayName)
        val emailError = validateEmail(email)
        val passwordError = validatePassword(password)
        val empty = setOf(
            displayName, email, password, _selectedAvatar.value.toString()
        ).any { it.isEmpty() }
        val error = setOf(
            displayNameError, emailError, passwordError
        ).any { it != null } || empty

        _formState.value = when {
            error -> SignupFormState(
                isDataValid = false,
                displayNameError = displayNameError,
                emailError = emailError,
                passwordError = passwordError
            )

            else -> SignupFormState(isDataValid = true)
        }
    }
}
