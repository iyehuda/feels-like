package com.iyehuda.feelslike.data

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.iyehuda.feelslike.data.model.UserDetails

class AuthRepository(val dataSource: AuthDataSource) {
    private val _userDetails = MutableLiveData<UserDetails?>().apply {
        value = dataSource.getUser()?.let { UserDetails.fromUser(it) }
    }
    val userDetails: LiveData<UserDetails?> = _userDetails

    fun logout() {
        dataSource.logout()
        _userDetails.value = null
    }

    suspend fun login(email: String, password: String): Result<UserDetails> {
        return dataSource.login(email, password).also(::onAuth)
    }

    suspend fun signup(
        name: String,
        email: String,
        password: String,
        avatar: Uri,
    ): Result<UserDetails> {
        return dataSource.signup(name, email, password, avatar).also(::onAuth)
    }

    private fun onAuth(authResult: Result<UserDetails>) {
        if (authResult.isSuccess) {
            _userDetails.value = authResult.getOrThrow()
        }
    }
}
