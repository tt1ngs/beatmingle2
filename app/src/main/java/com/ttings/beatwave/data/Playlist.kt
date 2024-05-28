package com.ttings.beatwave.data

import com.google.firebase.database.PropertyName
import com.google.type.DateTime

data class Playlist(
    val playlistId: String = "",
    val title: String = "",
    val image: String = "",
    val userId: String = "",
    val tracks: Map<String, String> = emptyMap(),
    val releaseDate: DateTime = DateTime.getDefaultInstance(),
    @PropertyName("pinned") val isPinned: Boolean = false,
    @PropertyName("private") val isPrivate: Boolean = false
)
