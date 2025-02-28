package com.iyehuda.feelslike.ui.signup

data class SignupFormState(
    val displayNameError: Int? = null,
    val emailError: Int? = null,
    val passwordError: Int? = null,
    val isDataValid: Boolean = false,
)
