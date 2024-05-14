package com.ttings.beatwave.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.ttings.beatwave.data.Playlist
import com.ttings.beatwave.data.Track
import com.ttings.beatwave.data.User
import com.ttings.beatwave.repositories.FirebasePlaylistRepository
import com.ttings.beatwave.repositories.TrackRepository
import com.ttings.beatwave.repositories.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SelectedPlaylistViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val trackRepository: TrackRepository,
    private val playlistRepository: FirebasePlaylistRepository
) : ViewModel() {

    private val _playlistTracks = MutableStateFlow<List<Track>>(emptyList())
    val playlistTracks: StateFlow<List<Track>> = _playlistTracks.asStateFlow()

    val playlist = MutableLiveData<Playlist?>()
    val creator = MutableLiveData<User?>()
    val isPlaylistLiked = MutableLiveData<Boolean>()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        fetchCurrentUser()
    }

    private fun fetchCurrentUser() {
        viewModelScope.launch {
            userRepository.getCurrentUser()?.let {
                _currentUser.value = it
            }
        }
    }

    suspend fun getAuthorById(id: String): User? {
        return userRepository.getAuthorById(id)
    }

    fun loadPlaylist(playlistId: String) {
        viewModelScope.launch {
            try {
                val playlistData = playlistRepository.getPlaylist(playlistId)
                playlist.value = playlistData
                creator.value = getAuthorById(playlistData!!.userId)

            } catch (e: Exception) {
                Timber.tag("OpenPlaylistViewModel").e(e, "Error loading playlist")
            }
        }
    }


    fun loadPlaylistTracks(playlistId: String) {
        viewModelScope.launch {
            try {
                val loadedTracks = mutableListOf<Track>()
                trackRepository.getTracksByPlaylist(playlistId).collect { newTracks ->
                    loadedTracks.addAll(newTracks)
                    _playlistTracks.value = loadedTracks.toList()
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load playlist tracks")
            }
        }
    }

    suspend fun getCurrentUser(): User? {
        return userRepository.getCurrentUser()
    }

    fun likePlaylist(playlistId: String) {
        viewModelScope.launch {
            try {
                val isLiked = playlistRepository.isPlaylistLiked(getCurrentUser()?.userId ?: "", playlistId)
                if (isLiked) {
                    playlistRepository.removePlaylistFromLibrary(getCurrentUser()?.userId ?: "", playlistId)
                    isPlaylistLiked.value = false
                } else {
                    playlistRepository.addPlaylistToLibrary(getCurrentUser()?.userId ?: "", playlistId)
                    isPlaylistLiked.value = true
                }
                isPlaylistLiked.value = !isLiked
            } catch (e: Exception) {
                Timber.tag("OpenPlaylistViewModel").e(e, "Error liking/unliking playlist")
            }
        }
    }

    fun playPlaylist(playlistId: String) {
        viewModelScope.launch {
            try {
                // TODO: добавить логику для воспроизведения плейлиста
            } catch (e: Exception) {
                Timber.tag("OpenPlaylistViewModel").e(e, "Error playing playlist")
            }
        }
    }

    fun shufflePlaylist(playlistId: String) {
        viewModelScope.launch {
            try {
                // TODO: добавить логику для перемешивания плейлиста
            } catch (e: Exception) {
                Timber.tag("OpenPlaylistViewModel").e(e, "Error shuffling playlist")
            }
        }
    }

    fun checkIfPlaylistLiked(playlistId: String) {
        viewModelScope.launch {
            try {
                val isLiked = playlistRepository.isPlaylistLiked(getCurrentUser()?.userId ?: "", playlistId)
                isPlaylistLiked.value = isLiked
            } catch (e: Exception) {
                Timber.tag("OpenPlaylistViewModel").e(e, "Error checking if playlist is liked")
            }
        }
    }

    fun secondsToMinutesSeconds(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }
}