package com.iyehuda.feelslike.ui.splash

import com.iyehuda.feelslike.data.auth.AuthRepository
import com.iyehuda.feelslike.ui.base.BaseAuthViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(authRepository: AuthRepository) :
    BaseAuthViewModel(authRepository)
