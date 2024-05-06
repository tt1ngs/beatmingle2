package com.ttings.beatwave.data

data class Playlist(
    val playlistId: String = "",
    val title: String = "",
    val image: String = "",
    val userId: String = "",
    val tracks: Map<String, String> = emptyMap(),
    val isPinned: Boolean = false,
    val isPrivate: Boolean = false
)
