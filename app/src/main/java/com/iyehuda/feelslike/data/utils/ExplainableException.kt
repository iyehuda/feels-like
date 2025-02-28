package com.iyehuda.feelslike.data.utils

import androidx.annotation.StringRes

class ExplainableException(
    @StringRes val errorStringRes: Int,
    message: String? = null,
    cause: Throwable? = null,
) : Exception(message, cause)

fun <T> Result<T>.explainableErrorOrNull(): Int? {
    return exceptionOrNull()?.let {
        if (it is ExplainableException) it.errorStringRes else null
    }
}
