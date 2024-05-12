package com.ttings.beatwave.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ttings.beatwave.data.Track
import com.ttings.beatwave.repositories.FirebasePlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SuggestionViewModel @Inject constructor(
    private val playlistRepository: FirebasePlaylistRepository
) : ViewModel() {
    val suggestedTracks = MutableLiveData<List<Track>>()
    private var currentPage = 0

    init {
        loadTracks()
    }

    private fun loadTracks() {
        Timber.tag("SuggestionViewModel").d("loadTracks called") // Добавьте эту строку
        viewModelScope.launch {
            try {
                val tracks = playlistRepository.getTracks()
                suggestedTracks.postValue(tracks)
                Timber.tag("SuggestionViewModel").d("Tracks loaded: $tracks")
                val currentTracks = suggestedTracks.value
                Timber.tag("SuggestionViewModel").d("Current suggestedTracks: $currentTracks")
            } catch (e: Exception) {
                Timber.tag("SuggestionViewModel").e(e, "Error loading initial tracks")
            }
        }
    }

    fun loadNextPage() {
        viewModelScope.launch {
            try {
                val newTracks = playlistRepository.getTracks()
                val currentTracks = suggestedTracks.value ?: emptyList()
                suggestedTracks.postValue(currentTracks + newTracks)
                currentPage++
            } catch (e: Exception) {
                Timber.tag("SuggestionViewModel").e(e, "Error loading next page")
            }
        }
    }

    fun addToPlaylist(trackId: String, playlistId: String) {
        viewModelScope.launch {
            try {
                playlistRepository.addTrackToPlaylist(trackId, playlistId)
            } catch (e: Exception) {
                Timber.tag("SuggestionViewModel").e(e, "Error adding track to playlist")
            }
        }
    }

    suspend fun replaceTrack(trackId: String) {
        val currentTracks = suggestedTracks.value ?: emptyList()
        val newTracks = currentTracks.filter { it.trackId != trackId }.toMutableList() // Remove the added track
        val nextTrack = playlistRepository.getTracks().firstOrNull() // Get the next track
        if (nextTrack != null && nextTrack !in newTracks) {
            newTracks.add(nextTrack) // Add the next track to the list
        }
        suggestedTracks.postValue(newTracks)
    }

    fun secondsToMinutesSeconds(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }
}