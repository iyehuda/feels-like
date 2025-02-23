package com.iyehuda.feelslike.ui.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.iyehuda.feelslike.data.AuthRepository
import com.iyehuda.feelslike.data.model.UserDetails
import com.iyehuda.feelslike.data.utils.Result

abstract class BaseAuthViewModel(val authRepository: AuthRepository) : BaseViewModel() {
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
        safeLaunch {
            authRepository.logout()
        }
    }
}
