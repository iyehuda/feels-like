package com.iyehuda.feelslike.data

import android.net.Uri
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.auth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.storage.storage
import com.iyehuda.feelslike.R
import com.iyehuda.feelslike.data.model.UserDetails
import com.iyehuda.feelslike.data.utils.ExplainableException
import com.iyehuda.feelslike.data.utils.Result
import kotlinx.coroutines.tasks.await

class AuthDataSource {
    fun getUser() = Firebase.auth.currentUser

    // TODO: Use kotlin builtin Result
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

    suspend fun signup(
        name: String,
        email: String,
        password: String,
        avatar: Uri,
    ): Result<UserDetails> {
        try {
            val authResult = Firebase.auth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user!!

            val storageRef = Firebase.storage.reference
            val avatarRef = storageRef.child("avatars/${user.uid}")

            avatarRef.putFile(avatar).await()
            val downloadUrl = avatarRef.downloadUrl.await()

            user.updateProfile(userProfileChangeRequest {
                displayName = name
                photoUri = downloadUrl
            }).await()

            return Result.Success(UserDetails.fromUser(user))
        } catch (e: FirebaseAuthUserCollisionException) {
            return Result.Error(ExplainableException(R.string.signup_email_taken, cause = e))
        } catch (e: Throwable) {
            return Result.Error(ExplainableException(R.string.signup_failed, cause = e))
        }
    }
}
