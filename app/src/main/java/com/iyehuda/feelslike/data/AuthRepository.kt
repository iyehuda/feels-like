package com.iyehuda.feelslike.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.iyehuda.feelslike.data.model.UserDetails
import com.iyehuda.feelslike.data.utils.Result

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */
class AuthRepository(val dataSource: AuthDataSource) {
    private val _userLogin = MutableLiveData<Result<UserDetails>?>()
    val userLogin: LiveData<Result<UserDetails>?> = _userLogin

    suspend fun logout() {
        dataSource.logout()
        _userLogin.value = null
    }

    suspend fun login(email: String, password: String) {
        _userLogin.value = dataSource.login(email, password)
    }
}
