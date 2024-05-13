package com.ttings.beatwave.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.ttings.beatwave.data.Playlist
import com.ttings.beatwave.data.Track
import com.ttings.beatwave.data.User
import com.ttings.beatwave.repositories.FirebasePlaylistRepository
import com.ttings.beatwave.repositories.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SelectedPlaylistViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val playlistRepository: FirebasePlaylistRepository
) : ViewModel() {

    val playlist = MutableLiveData<Playlist?>()
    val tracks = MutableLiveData<List<Track>>()
    val creator = MutableLiveData<User?>()
    val isPlaylistLiked = MutableLiveData<Boolean>()

    val user = liveData {
        emit(getCurrentUser())
    }

    fun loadPlaylist(playlistId: String) {
        viewModelScope.launch {
            try {
                val playlistData = playlistRepository.getPlaylist(playlistId)
                playlist.value = playlistData
                Timber.tag("OPV").d("Playlist loaded: $playlistData")
                Timber.tag("OpenPlaylistViewModel").d("Loading tracks for playlist: $playlistId")
                tracks.value = playlistRepository.getPlaylistTracks(playlistId)
                Timber.tag("OpenPlaylistViewModel").d("Tracks loaded: ${tracks.value}")

                creator.value = getAuthorById(playlistData!!.userId)


            } catch (e: Exception) {
                Timber.tag("OpenPlaylistViewModel").e(e, "Error loading playlist")
            }
        }
    }

    suspend fun getAuthorById(id: String): User? {
        return userRepository.getAuthorById(id)
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