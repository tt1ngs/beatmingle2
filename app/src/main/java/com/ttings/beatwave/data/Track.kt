package com.ttings.beatwave.data

data class Track(
    val trackId: String = "",
    val title: String = "",
    val genre: String = "",
    val image: String = "",
    val file: String = "",
    val artistIds: List<String> = listOf(),
    val albumId: String? = null,
    val duration: Int = 0,
    val isPinned: Boolean = false,
    val isPrivate: Boolean = false
)