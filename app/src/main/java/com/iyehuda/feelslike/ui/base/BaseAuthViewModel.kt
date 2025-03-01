package com.iyehuda.feelslike.ui.base

import com.iyehuda.feelslike.data.auth.AuthRepository

abstract class BaseAuthViewModel(protected val authRepository: AuthRepository) : BaseViewModel() {
    val userDetails = authRepository.userDetails

    fun logout() {
        safeLaunch {
            authRepository.logout()
        }
    }
}
