package com.ttings.beatwave.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ttings.beatwave.data.Track
import com.ttings.beatwave.data.User
import com.ttings.beatwave.repositories.TrackRepository
import com.ttings.beatwave.repositories.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val trackRepository: TrackRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uploadedTracks = MutableStateFlow<List<Track>>(emptyList())
    val uploadedTracks: StateFlow<List<Track>> = _uploadedTracks.asStateFlow()

    private val _likedTracks = MutableStateFlow<List<Track>>(emptyList())
    val likedTracks: StateFlow<List<Track>> = _likedTracks.asStateFlow()

    private val _playlistTracks = MutableStateFlow<List<Track>>(emptyList())
    val playlistTracks: StateFlow<List<Track>> = _playlistTracks.asStateFlow()

    private val _tracks = MutableStateFlow<List<Track>>(emptyList())
    val tracks: StateFlow<List<Track>> = _tracks.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _hasMoreData = MutableStateFlow(true)
    val hasMoreData: StateFlow<Boolean> = _hasMoreData.asStateFlow()

    val isPlaying: StateFlow<Boolean> = trackRepository.playbackState

    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()

    private val _currentProgress = MutableStateFlow(0f)
    val currentProgress: StateFlow<Float> = _currentProgress.asStateFlow()

    private val _isCurrentTrackLiked = MutableStateFlow(false)
    val isCurrentTrackLiked: StateFlow<Boolean> = _isCurrentTrackLiked.asStateFlow()

    fun togglePlayPause() {
        trackRepository.togglePlayPause()
    }

    init {
        fetchCurrentUser()
        viewModelScope.launch {
            trackRepository.progress.collect { progress ->
                _currentProgress.value = progress
            }
        }
        trackRepository.trackChangeCallback = object : TrackRepository.TrackChangeCallback {
            override fun onTrackChange(track: Track, queue: List<Track>) {
                playTrack(track, queue)
            }
        }
    }

    fun nextTrack() {
        Timber.d("Attempting to go to the next track.")
        try {
            _currentTrack.value?.let { currentTrack ->
                val currentIndex = _tracks.value.indexOf(currentTrack)
                if (currentIndex != -1 && currentIndex < _tracks.value.size - 1) {
                    val nextTrack = _tracks.value[currentIndex + 1]
                    playTrack(nextTrack, _tracks.value.drop(currentIndex + 1))
                }
                checkIfCurrentTrackIsLiked()
            }

        } catch (e: Exception) {
            Timber.e(e, "Failed to load liked tracks")
        }
    }

    fun previousTrack() {
        Timber.d("Attempting to go to the previous track.")
        _currentTrack.value?.let { currentTrack ->
            val currentIndex = _tracks.value.indexOf(currentTrack)
            if (currentIndex > 0) {
                val previousTrack = _tracks.value[currentIndex - 1]
                playTrack(previousTrack, _tracks.value.drop(currentIndex))
            }
        }
        checkIfCurrentTrackIsLiked()
    }


    private fun fetchCurrentUser() {
        viewModelScope.launch {
            userRepository.getCurrentUser()?.let {
                _currentUser.value = it
            }
        }
    }

    fun loadLikedTracks(userId: String, limit: Int = 20) {
        viewModelScope.launch {
            try {
                val loadedTracks = mutableListOf<Track>()
                trackRepository.getTracksByLikedTracks(userId, limit).collect { newTracks ->
                    loadedTracks.addAll(newTracks)
                    _likedTracks.value = loadedTracks.toList()
                    _tracks.value = _likedTracks.value
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load liked tracks")
            }
        }
    }

    fun loadUserUploads(userId: String, limit: Int = 20) {
        viewModelScope.launch {
            try {
                val loadedTracks = mutableListOf<Track>()
                trackRepository.getTracksByUserUploads(userId, limit).collect { newTracks ->
                    loadedTracks.addAll(newTracks)
                    _uploadedTracks.value = loadedTracks.toList()
                    _tracks.value = _uploadedTracks.value
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load user uploads")
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
                    _tracks.value = _playlistTracks.value
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load playlist tracks")
            }
        }
    }

    suspend fun getAuthorById(id: String): User? {
        return userRepository.getAuthorById(id)
    }

    fun secondsToMinutesSeconds(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }

    fun playTrack(track: Track, queue: List<Track>) {
        Timber.d("Setting current track: ${track.title}")
        _currentTrack.value = track
        try {
            viewModelScope.launch {
                trackRepository.setupMediaPlayer(track, queue)
            }
        } catch (e: Exception) {
            Timber.tag("PlayerViewModel").e(e, "Error playing track")
        }
        checkIfCurrentTrackIsLiked()
    }

    override fun onCleared() {
        try {
            super.onCleared()
            trackRepository.stopPlayback()
            trackRepository.cleanUp()
        } catch (e: Exception) {
            Timber.tag("PlayerViewModel").e(e, "Error stopping playback")
        }
    }

    fun toggleFavorite() {
        _currentTrack.value?.let { track ->
            _currentUser.value?.let { user ->
                viewModelScope.launch {
                    if (_isCurrentTrackLiked.value) {
                        trackRepository.removeTrackFromLibrary(track.trackId, user)
                        _isCurrentTrackLiked.value = false
                    } else {
                        trackRepository.addTrackToLibrary(track.trackId, user)
                        _isCurrentTrackLiked.value = true
                    }
                }
            }
        }
    }

    private fun checkIfCurrentTrackIsLiked() {
        _currentTrack.value?.let { track ->
            _currentUser.value?.let { user ->
                viewModelScope.launch {
                    _isCurrentTrackLiked.value = trackRepository.isTrackLikedByUser(track.trackId, user.userId)
                }
            }
        }
    }

}