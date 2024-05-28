package com.ttings.beatwave.viewmodels

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ttings.beatwave.data.Playlist
import com.ttings.beatwave.data.Track
import com.ttings.beatwave.data.User
import com.ttings.beatwave.repositories.TrackRepository
import com.ttings.beatwave.repositories.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val trackRepository: TrackRepository
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user = _user.asStateFlow()

    private val _followersCount = MutableStateFlow(0)
    val followersCount = _followersCount.asStateFlow()

    private val _followingCount = MutableStateFlow(0)
    val followingCount = _followingCount.asStateFlow()

    private val _isFollowing = MutableStateFlow(false)
    val isFollowing = _isFollowing.asStateFlow()

    private val _userUploads = MutableStateFlow<List<Track>>(emptyList())
    val userUploads = _userUploads.asStateFlow()

    private val _userPlaylists = MutableStateFlow<List<Playlist>>(emptyList())
    val userPlaylists = _userPlaylists.asStateFlow()

    private val _userAlbums = MutableStateFlow<List<Playlist>>(emptyList())
    val userAlbums = _userAlbums.asStateFlow()

    private val _userLikes = MutableStateFlow<List<Track>>(emptyList())
    val userLikes = _userLikes.asStateFlow()

    fun loadUser(userId: String) {
        viewModelScope.launch {
            _user.value = userRepository.getAuthorById(userId)
            _followersCount.value = userRepository.getFollowersCount()
            _followingCount.value = userRepository.getFollowingCount()
            _isFollowing.value = userRepository.isFollowing(userId)
            trackRepository.getTracksByUserUploads(userId).collect { _userUploads.value = it }
            trackRepository.getPlaylistsByUser(userId, "playlists").collect { _userPlaylists.value = it }
            trackRepository.getPlaylistsByUser(userId, "albums").collect { _userAlbums.value = it }
            trackRepository.getTracksByLikedTracks(userId).collect { _userLikes.value = it }
        }
    }

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    init {
        viewModelScope.launch {
            _currentUser.value = userRepository.getCurrentUser()
        }
    }

    fun onFollowButtonClick(userId: String) {
        if (isFollowing.value) {
            unfollowUser(userId)
        } else {
            followUser(userId)
        }
    }

    private fun followUser(userId: String) {
        viewModelScope.launch {
            userRepository.followUser(userId)
            _isFollowing.value = true
            // Предполагаем, что подписчики увеличиваются на 1
            _followersCount.value += 1
        }
    }

    private fun unfollowUser(userId: String) {
        viewModelScope.launch {
            userRepository.unfollowUser(userId)
            _isFollowing.value = false
            // Предполагаем, что подписчики уменьшаются на 1
            if (_followersCount.value > 0) _followersCount.value -= 1
        }
    }

    fun getCurrentUser(): User? {
        var currentUser: User? = null
        viewModelScope.launch {
            currentUser = userRepository.getCurrentUser()
        }
        return currentUser
    }

    // LiveData для отслеживания ошибок
    val error: MutableLiveData<String> = MutableLiveData()
    // Функция для загрузки изображения на Firebase Storage
    suspend fun uploadImage(uri: Uri) {
        val downloadUrl = userRepository.uploadImage(uri)
        userRepository.updateUserImage(downloadUrl)
    }
    // Функция для загрузки изображения на Firebase Storage
    suspend fun uploadBackground(uri: Uri) {
        val downloadUrl = userRepository.uploadBackground(uri)
        userRepository.updateUserBackground(downloadUrl)
    }

    // Функция для обновления имени пользователя
    suspend fun updateUserName(name: String) {
        if (name.isBlank()) {
            error.value = "Name cannot be blank"
            return
        }
        userRepository.updateUserName(name)
    }
}