package com.iyehuda.feelslike.data.model

import android.net.Uri
import com.google.firebase.auth.FirebaseUser

/**
 * Data class that captures user information for logged in users retrieved from AuthRepository
 */
data class UserDetails(
    val email: String,
    val displayName: String,
    val photoUrl: Uri,
) {
    companion object {
        fun fromUser(user: FirebaseUser) = UserDetails(
            user.email!!, user.displayName!!, user.photoUrl!!
        )
    }
}
