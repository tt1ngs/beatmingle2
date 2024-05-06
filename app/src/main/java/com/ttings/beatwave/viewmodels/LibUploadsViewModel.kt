package com.ttings.beatwave.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.ttings.beatwave.repositories.TrackRepository
import com.ttings.beatwave.repositories.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LibUploadsViewModel @Inject constructor(
    private val trackRepository: TrackRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    val uploadedTracks = liveData {
        try {
            val currentUser = userRepository.getCurrentUser()
            if (currentUser != null) {
                Timber.tag("UploadsViewModel").d("Getting uploaded tracks for user: ${currentUser.userId}")
                emit(trackRepository.getUploadedTracksByUser(currentUser.userId, currentUser.username ?: "Unknown"  ))
            }
            else{
                Timber.tag("UploadsViewModel").e("Current user is null")
            }
        } catch (e: Exception) {
            Timber.tag("UploadsViewModel").e(e, "Error getting uploaded tracks")
        }
    }


}