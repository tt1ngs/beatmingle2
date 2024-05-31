package com.ttings.beatwave.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ttings.beatwave.data.Playlist
import com.ttings.beatwave.data.Track
import com.ttings.beatwave.data.User
import com.ttings.beatwave.repositories.FirebasePlaylistRepository
import com.ttings.beatwave.repositories.TrackRepository
import com.ttings.beatwave.repositories.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val trackRepository: TrackRepository,
    private val userRepository: UserRepository,
    private val playlistRepository: FirebasePlaylistRepository
) : ViewModel() {

    val tracks = MutableLiveData<List<Track>>()
    val playlists = MutableLiveData<List<Playlist>>()
    val authors = MutableLiveData<List<User>>()

    init {
        loadTracks()
    }

    private fun loadTracks() = viewModelScope.launch {
        tracks.postValue(trackRepository.getPublicTracks())
    }

    fun loadPlaylistsAndAuthors() = viewModelScope.launch {
        playlists.postValue(playlistRepository.getPublicPlaylists())
        authors.postValue(userRepository.getAuthors())
        Timber.tag("HomeViewModel").d("Playlists: ${playlists.value}")
        Timber.tag("HomeViewModel").d("authors: ${authors.value}")
    }

    fun secondsToMinutesSeconds(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }
}
