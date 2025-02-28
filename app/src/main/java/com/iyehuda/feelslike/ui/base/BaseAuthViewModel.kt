package com.iyehuda.feelslike.ui.base

import com.iyehuda.feelslike.data.AuthRepository

abstract class BaseAuthViewModel(private val authRepository: AuthRepository) : BaseViewModel() {
    val userDetails = authRepository.userDetails

    fun logout() {
        safeLaunch {
            authRepository.logout()
        }
    }
}
