package com.iyehuda.feelslike.data.model

import android.net.Uri
import com.google.firebase.auth.FirebaseUser

data class UserDetails(
    val userId: String,
    val email: String,
    val displayName: String,
    val photoUrl: Uri,
) {
    companion object {
        fun fromUser(user: FirebaseUser) = UserDetails(
            user.uid, user.email!!, user.displayName!!, user.photoUrl!!
        )
    }
}
