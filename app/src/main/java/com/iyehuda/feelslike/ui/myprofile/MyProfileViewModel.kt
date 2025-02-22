package com.iyehuda.feelslike.ui.myprofile

import androidx.lifecycle.map
import com.iyehuda.feelslike.data.AuthRepository
import com.iyehuda.feelslike.ui.base.BaseAuthViewModel

class MyProfileViewModel(authRepository: AuthRepository) : BaseAuthViewModel(authRepository) {
    val text = userDetails.map {
        "This is ${it?.displayName ?: "Nobody"}'s profile"
    }
}
