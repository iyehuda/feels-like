package com.iyehuda.feelslike.data.utils

import android.util.Patterns
import com.iyehuda.feelslike.R

class FormValidator {
    companion object {
        private const val MIN_DISPLAY_NAME_LENGTH = 2
        private const val MIN_PASSWORD_LENGTH = 6

        fun validateDisplayName(name: String): Int? =
            if (name.isEmpty() || name.length >= MIN_DISPLAY_NAME_LENGTH) null else R.string.invalid_display_name

        fun validateEmail(email: String): Int? =
            if (email.isEmpty() || Patterns.EMAIL_ADDRESS.matcher(email).matches()) null else R.string.invalid_email

        fun validatePassword(password: String): Int? =
            if (password.isEmpty() || password.length >= MIN_PASSWORD_LENGTH) null else R.string.invalid_password
    }
}
