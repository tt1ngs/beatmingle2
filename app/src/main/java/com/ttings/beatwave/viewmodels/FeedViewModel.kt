package com.ttings.beatwave.viewmodels

import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.ttings.beatwave.data.Comment
import com.ttings.beatwave.data.Playlist
import com.ttings.beatwave.data.Track
import com.ttings.beatwave.data.User
import com.ttings.beatwave.repositories.FeedRepository
import com.ttings.beatwave.repositories.FirebasePlaylistRepository
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
    private val feedRepository: FeedRepository,
    private val userRepository: UserRepository,
    private val playlistRepository: FirebasePlaylistRepository
) : ViewModel() {

    val tracks = feedRepository.getTracks().cachedIn(viewModelScope)

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null
    var currentPlayingTrack: Track? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isUserFollowed = MutableStateFlow(false)
    val isUserFollowed: StateFlow<Boolean> = _isUserFollowed.asStateFlow()

    private val _isTrackLiked = MutableStateFlow(false)
    val isTrackLiked: StateFlow<Boolean> = _isTrackLiked.asStateFlow()

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments.asStateFlow()

    val playlists = mutableStateOf(listOf<Playlist>())

    init {
        fetchCurrentUser()
    }

    fun addTrackToPlaylist(trackId: String, playlistId: String) {
        viewModelScope.launch {
            playlistRepository.addTrackToPlaylist(trackId, playlistId)
        }
    }

    private fun fetchCurrentUser() {
        viewModelScope.launch {
            userRepository.getCurrentUser()?.let {
                _currentUser.value = it
            }
        }
    }

    fun fetchUserPlaylists(userId: String) {
        viewModelScope.launch {
            playlists.value = getUserPlaylists(userId)
        }
    }

    suspend fun getUserPlaylists(userId: String): List<Playlist> {
        return playlistRepository.getUserPlaylists(userId)
    }

    fun addComment(trackId: String, comment: String) {
        viewModelScope.launch {
            try {
                feedRepository.addComment(trackId, comment)
                loadComments(trackId)
            } catch (e: Exception) {
                Timber.tag("FeedViewModel").e(e, "Error adding comment")
            }
        }
    }

    fun loadComments(trackId: String) {
        viewModelScope.launch {
            feedRepository.getComments(trackId).collect { newComments ->
                _comments.value = newComments
            }
        }
    }

    suspend fun getUserById(userId: String): User? {
        return try {
            return feedRepository.getUserById(userId)
        } catch (e: Exception) {
            Timber.tag("FeedViewModel").e(e, "Error getting user by id")
            null
        }
    }

    suspend fun updateLikeState(trackId: String) {
        _isTrackLiked.value = isLiked(trackId)
    }

    suspend fun updateFollowState(userId: String) {
        _isUserFollowed.value = isFollowed(userId)
    }

    fun startPlayback(track: Track) {
        if (currentPlayingTrack == null) {
            currentPlayingTrack = track
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build())
                setDataSource(track.file)
                prepareAsync()
                setOnPreparedListener {
                    start()
                    _isPlaying.value = true
                }
            }
        } else if (currentPlayingTrack != track) {
            stopPlayback()
            startPlayback(track)
        } else {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.pause()
                    _isPlaying.value = false
                } else {
                    it.start()
                    _isPlaying.value = true
                }
            }
        }
    }

    fun stopPlayback() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        currentPlayingTrack = null
        _isPlaying.value = false
    }

    private suspend fun isLiked(trackId: String): Boolean {
        return try {
            feedRepository.isLiked(trackId)
        } catch (e: Exception) {
            Timber.tag("FeedViewModel").e(e, "Error checking if track is liked")
            false
        }
    }

    fun addTrackToLibrary(trackId: String) {
        viewModelScope.launch {
            try {
                feedRepository.addTrackToLibrary(trackId)
                updateLikeState(trackId)
            } catch (e: Exception) {
                Timber.tag("FeedViewModel").e(e, "Error adding track to library")
            }
        }
    }

    fun deleteTrackFromLibrary(trackId: String) {
        viewModelScope.launch {
            try {
                feedRepository.deleteTrackFromLibrary(trackId)
                updateLikeState(trackId)
            } catch (e: Exception) {
                Timber.tag("FeedViewModel").e(e, "Error deleting track from library")
            }
        }
    }

    private suspend fun isFollowed(userId: String): Boolean {
        _isUserFollowed.value = feedRepository.isFollowing(userId)
        return feedRepository.isFollowing(userId)
    }

    fun followUser(userId: String) {
        viewModelScope.launch {
            try {
                feedRepository.followUser(userId)
                updateFollowState(userId)
            } catch (e: Exception) {
                Timber.tag("FeedViewModel").e(e, "Error following user")
            }
        }
    }

    fun unfollowUser(userId: String) {
        viewModelScope.launch {
            try {
                feedRepository.unfollowUser(userId)
                updateFollowState(userId)
            } catch (e: Exception) {
                Timber.tag("FeedViewModel").e(e, "Error unfollowing user")
            }
        }
    }

    suspend fun getAuthorById(id: String): User? {
        return feedRepository.getAuthorById(id)
    }
}