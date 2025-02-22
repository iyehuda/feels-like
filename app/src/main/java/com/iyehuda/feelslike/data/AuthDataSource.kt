package com.iyehuda.feelslike.data

import com.iyehuda.feelslike.R
import com.iyehuda.feelslike.data.model.UserDetails
import com.iyehuda.feelslike.data.utils.ExplainableException
import com.iyehuda.feelslike.data.utils.Result
import kotlinx.coroutines.delay

/**
 * Class that handles authentication with login credentials and retrieves user information.
 */
class AuthDataSource {
    suspend fun login(email: String, password: String): Result<UserDetails> {
        try { // TODO: handle loggedInUser authentication
            delay(1000)
            if (email != "a@a.a" || password != "aaaaaa") {
                return Result.Error(ExplainableException(R.string.login_invalid_credentials))
            }
            val fakeUser = UserDetails(email, "Jane Doe")
            return Result.Success(fakeUser)
        } catch (e: Throwable) {
            return Result.Error(ExplainableException(R.string.login_failed, cause = e))
        }
    }

    suspend fun logout() { // TODO: revoke authentication
        delay(500)
    }
}
