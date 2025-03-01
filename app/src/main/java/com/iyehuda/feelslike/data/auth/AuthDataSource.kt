package com.iyehuda.feelslike.data.auth

import android.net.Uri
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.storage.storage
import com.iyehuda.feelslike.R
import com.iyehuda.feelslike.data.model.UserDetails
import com.iyehuda.feelslike.data.utils.ExplainableException
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthDataSource @Inject constructor() {
    fun getUser() = Firebase.auth.currentUser

    suspend fun login(email: String, password: String): Result<UserDetails> {
        try {
            val auth = Firebase.auth.signInWithEmailAndPassword(email, password).await()

            return Result.success(UserDetails.fromUser(auth.user!!))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            return Result.failure(
                ExplainableException(
                    R.string.login_invalid_credentials, cause = e
                )
            )
        } catch (e: Throwable) {
            return Result.failure(ExplainableException(R.string.login_failed, cause = e))
        }
    }

    fun logout() {
        Firebase.auth.signOut()
    }

    private suspend fun updateUserProfile(
        user: FirebaseUser,
        name: String,
        avatar: Uri,
    ) {
        val downloadUrl = if (user.photoUrl == avatar) {
            avatar
        } else {
            val storageRef = Firebase.storage.reference
            val avatarRef = storageRef.child("avatars/${user.uid}")

            avatarRef.putFile(avatar).await()

            avatarRef.downloadUrl.await()
        }

        user.updateProfile(userProfileChangeRequest {
            displayName = name
            photoUri = downloadUrl
        }).await()
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

            updateUserProfile(user, name, avatar)

            return Result.success(UserDetails.fromUser(user))
        } catch (e: FirebaseAuthUserCollisionException) {
            return Result.failure(ExplainableException(R.string.signup_email_taken, cause = e))
        } catch (e: Throwable) {
            return Result.failure(ExplainableException(R.string.signup_failed, cause = e))
        }
    }

    suspend fun updateProfile(name: String, avatar: Uri): Result<UserDetails> {
        try {
            val user = getUser()!!

            updateUserProfile(user, name, avatar)

            return Result.success(UserDetails.fromUser(user))
        } catch (e: Throwable) {
            return Result.failure(ExplainableException(R.string.update_profile_failed, cause = e))
        }
    }
}
