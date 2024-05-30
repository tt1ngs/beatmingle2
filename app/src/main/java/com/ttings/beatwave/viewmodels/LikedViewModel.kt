package com.ttings.beatwave.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ttings.beatwave.data.Track
import com.ttings.beatwave.data.User
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
class LikedViewModel @Inject constructor(
    private val trackRepository: TrackRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _likedTracks = MutableStateFlow<List<Track>>(emptyList())
    val likedTracks: StateFlow<List<Track>> = _likedTracks.asStateFlow()

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

    fun loadLikedTracks(userId: String, limit: Int = 20) {
        viewModelScope.launch {
            try {
                val loadedTracks = mutableListOf<Track>()
                trackRepository.getTracksByLikedTracks(userId, limit).collect { newTracks ->
                    loadedTracks.addAll(newTracks)
                    _likedTracks.value = loadedTracks.toList()
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load liked tracks")
            }
        }
    }

    fun secondsToMinutesSeconds(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }

    fun toggleTrackInLibrary(trackId: String, user: User) {
        viewModelScope.launch {
            try {
                val isTrackLikedByUser = trackRepository.isTrackLikedByUser(trackId, user.userId)
                if (isTrackLikedByUser) {
                    trackRepository.removeTrackFromLibrary(trackId, user)
                } else {
                    trackRepository.addTrackToLibrary(trackId, user)
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to toggle track in library")
            }
        }
    }
}