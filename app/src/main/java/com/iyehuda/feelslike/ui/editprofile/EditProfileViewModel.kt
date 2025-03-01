package com.iyehuda.feelslike.ui.editprofile

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.iyehuda.feelslike.data.auth.AuthRepository
import com.iyehuda.feelslike.data.model.UserDetails
import com.iyehuda.feelslike.data.utils.FormValidator.Companion.validateDisplayName
import com.iyehuda.feelslike.ui.base.BaseAuthViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(authRepository: AuthRepository) : BaseAuthViewModel(
    authRepository
) {
    private val _formState = MutableLiveData<EditProfileFormState>()
    val formState: LiveData<EditProfileFormState> = _formState
    private val _selectedAvatar = MutableLiveData(Uri.EMPTY)
    val selectedAvatar: LiveData<Uri> = _selectedAvatar

    fun updateProfile(
        name: String,
        avatar: Uri,
        callback: (Result<UserDetails>) -> Unit,
    ) = safeLaunch {
        val result = authRepository.updateProfile(name, avatar)
        callback(result)
    }

    fun updateAvatar(uri: Uri?) {
        _selectedAvatar.value = uri ?: Uri.EMPTY
    }

    fun updateFormData(displayName: String) {
        val displayNameError = validateDisplayName(displayName)
        val empty = setOf(displayName, _selectedAvatar.value.toString()).any { it.isEmpty() }
        val error = displayNameError != null || empty

        _formState.value = when {
            error -> EditProfileFormState(
                isDataValid = false,
                displayNameError = displayNameError,
            )

            else -> EditProfileFormState(isDataValid = true)
        }
    }
}
