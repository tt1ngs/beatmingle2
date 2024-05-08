package com.ttings.beatwave.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
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
class FeedViewModel @Inject constructor(
    private val trackRepository: TrackRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    val tracks = trackRepository.getTracks().cachedIn(viewModelScope)

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    init {
        viewModelScope.launch {
            _user.value = userRepository.getCurrentUser()
        }
    }

    suspend fun getAuthorById(id: String): User? {
        return userRepository.getAuthorById(id)
    }

    fun addTrackToLibrary(trackId: String) {
        viewModelScope.launch {
            try {
                trackRepository.addTrackToLibrary(trackId, user.value!!)
            } catch (e: Exception) {
                Timber.tag("FeedViewModel").e(e, "Error liking track")
            }
        }
    }

    fun deleteTrackFromLibrary(trackId: String) {
        viewModelScope.launch {
            try {
                trackRepository.removeTrackFromLibrary(trackId, user.value!!)
            } catch (e: Exception) {
                Timber.tag("FeedViewModel").e(e, "Error unliking track")
            }
        }
    }

    fun followUser(userId: String) {
        viewModelScope.launch {
            try {
                userRepository.followUser(userId)
            } catch (e: Exception) {
                Timber.tag("FeedViewModel").e(e, "Error following user")
            }
        }
    }

    fun unfollowUser(userId: String) {
        viewModelScope.launch {
            try {
                userRepository.unfollowUser(userId)
            } catch (e: Exception) {
                Timber.tag("FeedViewModel").e(e, "Error unfollowing user")
            }
        }
    }

    suspend fun isFollowing(userId: String): Boolean {
        return userRepository.isFollowing(userId)
    }

    suspend fun isLiked(trackId: String): Boolean {
        return trackRepository.isLiked(user.value!!, trackId)
    }
}