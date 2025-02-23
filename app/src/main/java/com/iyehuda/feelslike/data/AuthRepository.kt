package com.iyehuda.feelslike.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.iyehuda.feelslike.data.model.UserDetails
import com.iyehuda.feelslike.data.utils.Result

class AuthRepository(val dataSource: AuthDataSource) {
    private val _userLogin = MutableLiveData<Result<UserDetails>?>().apply {
        Firebase.auth.currentUser?.let {
            value = Result.Success(UserDetails.fromUser(it))
        }
    }
    val userLogin: LiveData<Result<UserDetails>?> = _userLogin

    fun logout() {
        dataSource.logout()
        _userLogin.value = null
    }

    suspend fun login(email: String, password: String) {
        _userLogin.value = dataSource.login(email, password)
    }
}
