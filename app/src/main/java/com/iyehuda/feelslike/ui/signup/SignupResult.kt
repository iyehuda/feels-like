package com.iyehuda.feelslike.ui.signup

import com.iyehuda.feelslike.data.model.UserDetails

data class SignupResult(
    val success: UserDetails? = null,
    val error: Int? = null,
)
