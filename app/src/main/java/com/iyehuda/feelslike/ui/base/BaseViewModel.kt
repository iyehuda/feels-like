package com.iyehuda.feelslike.ui.base

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

abstract class BaseViewModel() : ViewModel() {
    fun safeLaunch(block: suspend CoroutineScope.() -> Unit) = viewModelScope.launch(
        CoroutineExceptionHandler { _, throwable ->
            Log.d(ERROR_TAG, throwable.message.orEmpty())
        }, block = block
    )

    companion object {
        private const val ERROR_TAG = "FEELS LIKE ERROR"
    }
}
