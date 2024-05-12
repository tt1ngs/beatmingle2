package com.ttings.beatwave.viewmodels

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ttings.beatwave.data.User
import com.ttings.beatwave.repositories.TrackRepository
import com.ttings.beatwave.repositories.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UploadViewModel @Inject constructor(
    private val trackRepository: TrackRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    init {
        viewModelScope.launch {
            _currentUser.value = userRepository.getCurrentUser()
        }
    }

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun getAudioFileDuration(context: Context, audioUri: Uri): Long {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, audioUri)
        val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        retriever.release()
        return (duration?.toLong()?.div(1000)) ?: 0
    }

    fun uploadTrack(
        trackId: String,
        title: String,
        genre: String,
        artistIds: List<String>,
        albumId: String?,
        duration: Int,
        imageUri: String,
        trackUri: String,
        isPrivate: Boolean
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                trackRepository.uploadTrack(
                    currentUser.value!!.userId,
                    trackId,
                    title,
                    genre,
                    artistIds,
                    albumId,
                    duration,
                    imageUri,
                    trackUri,
                    isPrivate
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    private val _genres = MutableStateFlow<List<String>>(emptyList())
    val genres: StateFlow<List<String>> = _genres

    init {
        viewModelScope.launch {
            _genres.value = trackRepository.getGenres()
        }
    }
}