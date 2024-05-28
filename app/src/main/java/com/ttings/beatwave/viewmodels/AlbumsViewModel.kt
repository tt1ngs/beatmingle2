package com.ttings.beatwave.viewmodels

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.ttings.beatwave.data.Playlist
import com.ttings.beatwave.data.User
import com.ttings.beatwave.repositories.FirebaseAlbumsRepository
import com.ttings.beatwave.repositories.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AlbumsViewModel @Inject constructor(
    private val repository: FirebaseAlbumsRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    init {
        viewModelScope.launch {
            _currentUser.value = userRepository.getCurrentUser()
        }
    }

    suspend fun getAuthorStatus(): Boolean {
        return userRepository.getAuthorStatus()
    }

    val likedAlbums: LiveData<Set<Pair<Playlist, String>>> = liveData {
        val currentUser = userRepository.getCurrentUser()
        val data = repository.getLikedAlbums(currentUser!!.userId).toSet()
        emit(data)
    }

    val uploadedAlbums: LiveData<Set<Pair<Playlist, String>>> = liveData {
        val currentUser = userRepository.getCurrentUser()
        val data = repository.getUploadedAlbums(currentUser!!.userId).toSet()
        emit(data)
    }

    val albums: LiveData<Set<Pair<Playlist, String>>> = liveData {
        val currentUser = userRepository.getCurrentUser()
        val likedData = repository.getLikedAlbums(currentUser!!.userId).toSet()
        val uploadedData = repository.getUploadedAlbums(currentUser.userId).toSet()
        emit(likedData + uploadedData)
    }

    fun saveAlbum(title: String, imageUri: Uri, userId: String, isPrivate: Boolean) {
        _isLoading.value = true
        try {
            viewModelScope.launch {
                val imageUrl = repository.uploadImage(imageUri)
                val playlist = Playlist(
                    UUID.randomUUID().toString(),
                    title,
                    imageUrl,
                    userId,
                    isPrivate = isPrivate
                )
                repository.createAlbum(playlist, imageUri)
            }
        } finally {
            _isLoading.value = false
        }
    }

}
