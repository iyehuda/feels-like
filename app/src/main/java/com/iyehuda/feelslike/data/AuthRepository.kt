package com.iyehuda.feelslike.data

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.iyehuda.feelslike.data.model.UserDetails
import com.iyehuda.feelslike.data.utils.Result

class AuthRepository(val dataSource: AuthDataSource) {
    // TODO: Split userLogin into two separate LiveData objects (one for UserDetails and one for errors)
    private val _userLogin = MutableLiveData<Result<UserDetails>?>().apply {
        // TODO: Move this logic to the AuthDataSource to abstract Firebase
        value = Firebase.auth.currentUser?.let { Result.Success(UserDetails.fromUser(it)) }
    }
    val userLogin: LiveData<Result<UserDetails>?> = _userLogin

    fun logout() {
        dataSource.logout()
        _userLogin.value = null
    }

    suspend fun login(email: String, password: String) {
        _userLogin.value = dataSource.login(email, password)
    }

    suspend fun signup(name: String, email: String, password: String, avatar: Uri) {
        _userLogin.value = dataSource.signup(name, email, password, avatar)
    }
}
