package com.iyehuda.feelslike.data

import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.iyehuda.feelslike.R
import com.iyehuda.feelslike.data.model.UserDetails
import com.iyehuda.feelslike.data.utils.ExplainableException
import com.iyehuda.feelslike.data.utils.Result
import kotlinx.coroutines.tasks.await

class AuthDataSource {
    suspend fun login(email: String, password: String): Result<UserDetails> {
        try {
            val auth = Firebase.auth.signInWithEmailAndPassword(email, password).await()

            return Result.Success(UserDetails.fromUser(auth.user!!))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            return Result.Error(ExplainableException(R.string.login_invalid_credentials, cause = e))
        } catch (e: Throwable) {
            return Result.Error(ExplainableException(R.string.login_failed, cause = e))
        }
    }

    fun logout() {
        Firebase.auth.signOut()
    }
}
