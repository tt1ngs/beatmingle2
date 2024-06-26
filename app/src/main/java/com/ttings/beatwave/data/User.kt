package com.ttings.beatwave.data

import com.google.firebase.database.PropertyName

data class User(
    val userId: String = "",
    val username: String? = null,
    val avatar: String? = null,
    val background: String? = null,
    val profileSetupComplete: Boolean = false,
    @PropertyName("isAuthor") val isAuthor: Boolean = false,
    val likedPlaylists: List<String?> = emptyList(),
    val likedTracks: List<String?> = emptyList()
)