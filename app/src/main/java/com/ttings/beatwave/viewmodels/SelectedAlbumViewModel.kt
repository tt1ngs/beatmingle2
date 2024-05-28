package com.ttings.beatwave.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ttings.beatwave.data.Playlist
import com.ttings.beatwave.data.Track
import com.ttings.beatwave.data.User
import com.ttings.beatwave.repositories.FirebaseAlbumsRepository
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
class SelectedAlbumViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val trackRepository: TrackRepository,
    private val albumRepository: FirebaseAlbumsRepository
) : ViewModel() {

    private val _albumTracks = MutableStateFlow<List<Track>>(emptyList())
    val albumTracks: StateFlow<List<Track>> = _albumTracks.asStateFlow()

    val album = MutableLiveData<Playlist?>()
    val creator = MutableLiveData<User?>()
    val isAlbumLiked = MutableLiveData<Boolean>()

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

    fun loadAlbum(playlistId: String) {
        viewModelScope.launch {
            try {
                val albumData = albumRepository.getAlbum(playlistId)
                album.value = albumData
                creator.value = getAuthorById(albumData!!.userId)

            } catch (e: Exception) {
                Timber.tag("OpenPlaylistViewModel").e(e, "Error loading playlist")
            }
        }
    }


    fun loadAlbumTracks(playlistId: String) {
        viewModelScope.launch {
            try {
                val loadedTracks = mutableListOf<Track>()
                trackRepository.getTracksByAlbum(playlistId).collect { newTracks ->
                    loadedTracks.addAll(newTracks)
                    _albumTracks.value = loadedTracks.toList()
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load playlist tracks")
            }
        }
    }

    suspend fun getCurrentUser(): User? {
        return userRepository.getCurrentUser()
    }

    fun likeAlbum(playlistId: String) {
        viewModelScope.launch {
            try {
                val isLiked = albumRepository.isAlbumLiked(getCurrentUser()?.userId ?: "", playlistId)
                if (isLiked) {
                    albumRepository.removeAlbumFromLibrary(getCurrentUser()?.userId ?: "", playlistId)
                    isAlbumLiked.value = false
                } else {
                    albumRepository.addAlbumToLibrary(getCurrentUser()?.userId ?: "", playlistId)
                    isAlbumLiked.value = true
                }
                isAlbumLiked.value = !isLiked
            } catch (e: Exception) {
                Timber.tag("OpenPlaylistViewModel").e(e, "Error liking/unliking playlist")
            }
        }
    }

    fun playAlbum(playlistId: String) {
        viewModelScope.launch {
            try {
                // TODO: добавить логику для воспроизведения плейлиста
            } catch (e: Exception) {
                Timber.tag("OpenPlaylistViewModel").e(e, "Error playing playlist")
            }
        }
    }

    fun shuffleAlbum(playlistId: String) {
        viewModelScope.launch {
            try {
                // TODO: добавить логику для перемешивания плейлиста
            } catch (e: Exception) {
                Timber.tag("OpenPlaylistViewModel").e(e, "Error shuffling playlist")
            }
        }
    }

    fun checkIfAlbumLiked(playlistId: String) {
        viewModelScope.launch {
            try {
                val isLiked = albumRepository.isAlbumLiked(getCurrentUser()?.userId ?: "", playlistId)
                isAlbumLiked.value = isLiked
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
