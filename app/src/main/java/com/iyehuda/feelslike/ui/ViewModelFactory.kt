package com.iyehuda.feelslike.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.iyehuda.feelslike.ui.splash.SplashViewModel
import com.iyehuda.feelslike.data.AuthDataSource
import com.iyehuda.feelslike.data.AuthRepository
import com.iyehuda.feelslike.ui.login.LoginViewModel
import com.iyehuda.feelslike.ui.myprofile.MyProfileViewModel

class ViewModelFactory : ViewModelProvider.Factory {
    companion object {
        val authRepository = AuthRepository(
            dataSource = AuthDataSource()
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(SplashViewModel::class.java) -> SplashViewModel(
            authRepository = authRepository
        ) as T

        modelClass.isAssignableFrom(LoginViewModel::class.java) -> LoginViewModel(
            authRepository = authRepository
        ) as T

        modelClass.isAssignableFrom(MyProfileViewModel::class.java) -> MyProfileViewModel(
            authRepository = authRepository
        ) as T

        else -> throw IllegalArgumentException("Unknown ViewModel class")
    }
}
