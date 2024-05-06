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
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LikedViewModel @Inject constructor(
    private val trackRepository: TrackRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _likedTracks = MutableLiveData<List<Track>>()
    val likedTracks: LiveData<List<Track>> get() = _likedTracks

    suspend fun getAuthorById(id: String): User? {
        return userRepository.getAuthorById(id)
    }

    suspend fun getLikedTracks() {
        val currentUser = userRepository.getCurrentUser()
        if (currentUser != null) {
            viewModelScope.launch {
                try {
                    _likedTracks.value = trackRepository.getLikedTracksByUser(currentUser.userId)
                } catch (e: Exception) {
                    Timber.tag("LikedViewModel").e(e, "Failed to get liked tracks")
                }
            }
        }
    }

    fun secondsToMinutesSeconds(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }
}