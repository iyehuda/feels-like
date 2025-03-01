package com.iyehuda.feelslike.data.auth

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.iyehuda.feelslike.data.model.UserDetails
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(private val auth: AuthDataSource) {
    private val _userDetails = MutableLiveData<UserDetails?>().apply {
        value = auth.getUser()?.let { UserDetails.fromUser(it) }
    }
    val userDetails: LiveData<UserDetails?> = _userDetails

    fun logout() {
        auth.logout()
        _userDetails.value = null
    }

    suspend fun login(email: String, password: String): Result<UserDetails> {
        return auth.login(email, password).also(::onAuth)
    }

    suspend fun signup(
        name: String,
        email: String,
        password: String,
        avatar: Uri,
    ): Result<UserDetails> {
        return auth.signup(name, email, password, avatar).also(::onAuth)
    }

    suspend fun updateProfile(name: String, avatar: Uri): Result<UserDetails> {
        return auth.updateProfile(name, avatar).also(::onAuth)
    }

    private fun onAuth(authResult: Result<UserDetails>) {
        if (authResult.isSuccess) {
            _userDetails.value = authResult.getOrThrow()
        }
    }
}
