package com.ttings.beatwave.data

import com.google.firebase.auth.FirebaseUser

sealed class AuthState {
    data object Empty : AuthState()
    data class Loading(val message: String) : AuthState()
    data class Success(val user: FirebaseUser) : AuthState()
    data class Error(val error: String) : AuthState()
}
