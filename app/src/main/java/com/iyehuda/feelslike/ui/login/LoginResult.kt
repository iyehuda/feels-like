package com.iyehuda.feelslike.ui.login

import com.iyehuda.feelslike.data.model.UserDetails

data class LoginResult(
    val success: UserDetails? = null,
    val error: Int? = null,
)
