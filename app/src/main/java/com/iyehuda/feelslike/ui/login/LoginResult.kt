package com.iyehuda.feelslike.ui.login

import com.iyehuda.feelslike.data.model.UserDetails

/**
 * Authentication result : success (user details) or error message.
 */
data class LoginResult(
    val success: UserDetails? = null,
    val error: Int? = null,
)
