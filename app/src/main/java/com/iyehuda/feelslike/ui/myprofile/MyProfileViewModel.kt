package com.iyehuda.feelslike.ui.myprofile

import com.iyehuda.feelslike.data.AuthRepository
import com.iyehuda.feelslike.ui.base.BaseAuthViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MyProfileViewModel @Inject constructor(authRepository: AuthRepository) :
    BaseAuthViewModel(authRepository)
