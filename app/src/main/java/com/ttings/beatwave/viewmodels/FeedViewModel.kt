package com.ttings.beatwave.viewmodels

import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.ttings.beatwave.data.Comment
import com.ttings.beatwave.data.Track
import com.ttings.beatwave.data.User
import com.ttings.beatwave.repositories.FeedRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val feedRepository: FeedRepository
) : ViewModel() {
    val tracks = feedRepository.getTracks().cachedIn(viewModelScope)

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

    fun addComment(trackId: String, comment: String) {
        viewModelScope.launch {
            try {
                feedRepository.addComment(trackId, comment)
                getComments(trackId).collect {
                    _comments.value = it
                }
            } catch (e: Exception) {
                Timber.tag("FeedViewModel").e(e, "Error adding comment")
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

    fun getComments(trackId: String): Flow<List<Comment>> {
        return feedRepository.getComments(trackId)
    }

    suspend fun updateLikeState(trackId: String) {
        _isTrackLiked.value = isLiked(trackId)
    }

    suspend fun updateFollowState(userId: String) {
        _isUserFollowed.value = isFollowed(userId)
    }

    // Проверка наличия текущего трека перед воспроизведением
    fun startPlayback(track: Track) {
        if (currentPlayingTrack == null) {
            currentPlayingTrack = track
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build())
                setDataSource(track.file)
                prepareAsync() // Используем асинхронную подготовку
                setOnPreparedListener {
                    start()
                    _isPlaying.value = true
                }
            }
        } else if (currentPlayingTrack != track) {
            // Если текущий трек не совпадает с треком для воспроизведения, останавливаем проигрывание и воспроизводим новый трек
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

    // Остановка воспроизведения трека
    fun stopPlayback() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        currentPlayingTrack = null
        _isPlaying.value = false
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    suspend fun getAuthorById(id: String): User? {
        return feedRepository.getAuthorById(id)
    }

    fun addTrackToLibrary(trackId: String) {
        viewModelScope.launch {
            try {
                feedRepository.addTrackToLibrary(trackId)
                updateLikeState(trackId)
            } catch (e: Exception) {
                Timber.tag("FeedViewModel").e(e, "Error liking track")
            }
        }
    }

    fun deleteTrackFromLibrary(trackId: String) {
        viewModelScope.launch {
            try {
                feedRepository.deleteTrackFromLibrary(trackId)
                updateLikeState(trackId)
            } catch (e: Exception) {
                Timber.tag("FeedViewModel").e(e, "Error unliking track")
            }
        }
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

    private suspend fun isFollowed(userId: String): Boolean {
        _isUserFollowed.value = feedRepository.isFollowing(userId)
        return feedRepository.isFollowing(userId)
    }

    private suspend fun isLiked(trackId: String): Boolean {
        _isTrackLiked.value = feedRepository.isLiked(trackId)
        return feedRepository.isLiked(trackId)
    }
}