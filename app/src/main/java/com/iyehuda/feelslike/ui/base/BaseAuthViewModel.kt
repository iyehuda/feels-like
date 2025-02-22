package com.iyehuda.feelslike.ui.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.iyehuda.feelslike.data.AuthRepository
import com.iyehuda.feelslike.data.model.UserDetails
import com.iyehuda.feelslike.data.utils.Result
import kotlinx.coroutines.launch

abstract class BaseAuthViewModel(val authRepository: AuthRepository) : ViewModel() {
    val userDetails: LiveData<UserDetails?> = authRepository.userLogin.map { result ->
        when (result) {
            is Result.Success -> {
                result.data
            }

            else -> {
                null
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}
