package com.iyehuda.feelslike.data.auth

import android.net.Uri
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.iyehuda.feelslike.R
import com.iyehuda.feelslike.data.model.UserDetails
import com.iyehuda.feelslike.data.utils.ExplainableException
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthDataSource @Inject constructor() {
    private val auth = Firebase.auth
    private val storage = Firebase.storage

    fun getUser() = auth.currentUser

    suspend fun login(email: String, password: String): Result<UserDetails> {
        try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            return Result.success(UserDetails.fromUser(authResult.user!!))
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
        auth.signOut()
    }

    private suspend fun updateUserProfile(
        user: FirebaseUser, name: String, avatar: Uri
    ) {
        val imageRef = Firebase.storage.reference.child("avatars/${user.uid}.jpg")
        imageRef.putFile(avatar).await()
        val downloadUrl = imageRef.downloadUrl.await()

        val profileUpdates = userProfileChangeRequest {
            displayName = name
            photoUri = downloadUrl
        }
        user.updateProfile(profileUpdates).await()
    }

    suspend fun signup(
        name: String,
        email: String,
        password: String,
        avatar: Uri,
    ): Result<UserDetails> {
        try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
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
